angular.module('doxdbApp', [
    'doxdb.module', 'ngRoute', 'ui.bootstrap'
])

.config(function($compileProvider, $routeProvider) {

    $compileProvider.debugInfoEnabled(true);

    $routeProvider

    .when("/welcome", {
        templateUrl : "partials/welcome.html",
        controller : "DoxDBWelcomeController",
        controllerAs : "doxdbWelcome"
    })

    .when("/venue", {
        templateUrl : "partials/schema.html",
        controller : "DoxDBSchemaController",
        controllerAs : "doxdbSchema",
        resolve : {
            "resource" : "DoxDBvenue"
        }
    })

    .otherwise("/welcome");

})

.run(function($rootScope, $location, $timeout) {

    $rootScope.$on('$viewContentLoaded', function() {

        $timeout(function() {

            componentHandler.upgradeAllRegistered();
        });
    });
})

.controller('DoxDBNavController', function(DoxDBService) {

    navController = this;

    navController.schemas = DoxDBService.schemas;
})

.controller('DoxDBSchemaController', function(DoxDBService, $routeParams, $log, resource) {

    schemaController = this;
    schemaController.uiRowModel = {};

    resource.query({}, function(items) {

        schemaController.items = items;
    });

    schemaController.toggleDetails = function(_id) {

        if (schemaController.uiRowModel[_id] === undefined) {
            schemaController.uiRowModel[_id] = {
                shown : false
            };
        }
        schemaController.uiRowModel[_id].shown = !schemaController.uiRowModel[_id].shown;
    };

    schemaController.edit = function(_id) {

        $log.info("open editor for " + _id);

    };

    schemaController.trash = function(_id) {

        $log.info("delete confirmation for " + _id);

    };

})

.controller('DoxDBWelcomeController', function(DoxDBService, $log) {

    welcomeController = this;
    welcomeController.collapsed = true;
    welcomeController.foo = function() {

        $log.info("HERE");
    }

})

.controller('AccordionTableController', function() {

    accordionTable = this;
    accordionTable.groups = [
        {
            details : []
        }
    ];
    accordionTable.currentGroup = accordionTable.groups[0];

    this.addDataRow = function(dataRowScope) {

        // this is added for the data row which is used to trigger an expansion.
        if (accordionTable.currentGroup.data !== undefined) {
            accordionTable.groups.push({
                details : []
            });
            accordionTable.currentGroup = accordionTable.groups[accordionTable.groups.length - 1];
        }
        accordionTable.currentGroup.data = dataRowScope;
        dataRowScope.$on('$destroy', function(event) {

            accordionTable.removeGroup(dataRowScope);
        });
    };

    this.addDetailRow = function(detailRowScope) {

        // this is expected to be executed after a data row.  There can be more than one detail row.
        accordionTable.currentGroup.details.push(detailRowScope);
    };

    // This is called from the accordion-group directive when to remove itself
    this.removeGroup = function(rowScope) {

        var filteredGroups = [];
        angular.forEach(accordionTable.groups, function(group) {

            if (group.data != rowScope) {
                filteredGroups.push(group);
            }
        });
        accordionTable.groups = filteredGroups;
    };

})

// The accordion directive simply sets up the directive controller
// and adds an accordion CSS class to itself element.
.directive('accordionTable', function() {

    return {
        controller : 'AccordionTableController',
        controllerAs : 'accordionTable',
        transclude : true,
        link : function(scope, el, iAttrs, controller, transcludeFn) {

            transcludeFn(scope, function(clonedTranscludedContent) {

                el.append(clonedTranscludedContent);
            });

        }

    };
})

.directive('accordionTableDataRow', function($log, $compile) {

    return {
        transclude : true,
        xscope : {},
        require : "^accordionTable",

        xcompile : function(templateElement, templateAttributes) {

            var templateDirectiveContent = templateElement.contents().remove();
            var compiledContent = null;

            return function($scope, linkElement, linkAttributes) {

                /*
                 * This verification avoid to compile the content to all
                 * siblings, because when you compile the siblings, don't work
                 * (I don't know why, yet). So, doing this we get only the top
                 * level link function (from each iteration)
                 */
                if (!compiledContent) {
                    compiledContent = $compile(templateDirectiveContent);
                }

                /*
                 * Calling the link function passing the actual scope we get a
                 * clone object wich contains the finish viewed object, the view
                 * itself, the DOM!! Then, we attach the new dom in the element
                 * wich contains the directive
                 */
                compiledContent($scope, function(clone) {

                    linkElement.append(clone);
                });
            };
        },
        replace : true,
        template : function(tElement, tAttr) {

            tElement.attr("ng-click", "doxdbWelcome.foo()");
            tElement.attr("ng-transclude", "");
            if (tAttr.$attr.accordionTableDataRow === undefined) {
                return "<" + tElement[0].outerHTML.replace(/(^<\w+|\w+>$)/g, 'div') + ">";
            } else {
                tElement.removeAttr(tAttr.$attr.accordionTableDataRow);
                $log.info(tElement[0].outerHTML);
                return tElement[0].outerHTML;
            }
        },

        xxxcompile : function(tElement, tAttrs, transclude) {

            tAttrs.$set("ngClick", "doxdbWelcome.foo()");
            delete tAttrs['accordionTableDataRow'];
            return function(scope, el, iAttrs, controller, transcludeFn) {

                var rec = $compile(tElement);
                //$compile(el)(scope);
                rec(scope);
                transcludeFn(scope, function(clonedTranscludedContent) {

                    scope.isOpen = false;
                    $log.info(el);
                    el.append(clonedTranscludedContent);
                });

                //var c = $compile(el.contents());
                //c(scope);

                //$compile(el);//(scope);
                //                $compile(el);
            };
        },
        xxlink : {
            pre : function(scope, el, iAttrs, controller) {

                el.attr("ng-click", "doxdbWelcome.foo()");
                el.removeAttr("x-accordion-table-data-row");
                $compile(el)(scope);
                //el.attr("ng-click", "doxdbWelcome.foo()");
                //$compile(el)(scope);
                //                $log.info(el);
            },
            post : function(scope, el, iAttrs, controller, transcludeFn) {

                //$compile(el)(scope);
                transcludeFn(scope, function(clonedTranscludedContent) {

                    scope.isOpen = false;
                    $log.info(el);
                    el.append(clonedTranscludedContent);
                });

                //var c = $compile(el.contents());
                //c(scope);

                //$compile(el);//(scope);
                //                $compile(el);
            }
        }
    };
})

.service('DoxDBService', function() {

    this.schemas = [
        {
            "name" : "Venue",
            "schema" : "venue",
            "readAll" : true
        }, {
            "name" : "Horse",
            "schema" : "horse",
            "readAll" : false
        }
    ];

});
