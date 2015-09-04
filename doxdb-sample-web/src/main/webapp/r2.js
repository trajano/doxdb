angular.module('doxdbApp', [
    'doxdb.module', 'ngRoute', 'ngAnimate', 'ui.bootstrap'
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
    welcomeController.hello = "Hello world";
    welcomeController.foo = function() {

        $log.info("HERE");
    };

})

.controller('AccordionTableController', function($log) {

    accordionTable = this;
    accordionTable.groups = [
        {
            details : []
        }
    ];
    accordionTable.currentGroup = accordionTable.groups[0];

    this.addDataRow = function(dataRowScope) {

        $log.info(dataRowScope);
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

    this.getDataRow = function() {

        return accordionTable.currentGroup.data;
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
        scope : true,
        controller : 'AccordionTableController',
        controllerAs : 'accordionTable',

    };
})

.directive('accordionTableDataRow', function($log, $compile) {

    return {
        transclude : true,
        require : "^accordionTable",
        replace : true,
        scope : true,
        controller : function($scope, $log) {

            row = this;
            row.isOpen = false;

            row.toggle = function() {

                row.isOpen = !row.isOpen;
                $log.info(row.isOpen);
            };
        },
        controllerAs : 'row',
        template : function(tElement, tAttr) {

            tElement.attr("ng-click", "row.toggle()");
            tElement.attr("ng-transclude", "");
            if (tAttr.$attr.accordionTableDataRow === undefined) {
                return "<" + tElement[0].outerHTML.replace(/(^<\w+|\w+>$)/g, 'div') + ">";
            } else {
                tElement.removeAttr(tAttr.$attr.accordionTableDataRow);
                return tElement[0].outerHTML;
            }
        },
        link : function(scope, iElement, iAttrs, accordionTable) {

            accordionTable.addDataRow(scope);
            scope.$watch('row.isOpen', function(isOpen) {

                if (isOpen) {

                }
            });
        }
    };
})

.directive('accordionTableDetailRow', function($log, $compile, $animate) {

    return {
        require : "^accordionTable",
        replace : true,
        link : function(scope, element, iAttrs, accordionTable) {

            function expand() {

                element.removeClass('collapse').addClass('collapsing').attr('aria-expanded', true).attr('aria-hidden', false);

                $animate.addClass(element, 'in', {
                    to : {
                        height : element[0].scrollHeight + 'px'
                    }
                }).then(expandDone);
            }

            function expandDone() {

                element.removeClass('collapsing');
                element.css({
                    height : 'auto'
                });
            }

            function collapse() {

                if (!element.hasClass('collapse') && !element.hasClass('in')) {
                    return collapseDone();
                }

                element
                // IMPORTANT: The height must be set before adding "collapsing" class.
                // Otherwise, the browser attempts to animate from height 0 (in
                // collapsing class) to the given height here.
                .css({
                    height : element[0].scrollHeight + 'px'
                })
                // initially all panel collapse have the collapse class, this removal
                // prevents the animation from jumping to collapsed state
                .removeClass('collapse').addClass('collapsing').attr('aria-expanded', false).attr('aria-hidden', true);

                $animate.removeClass(element, 'in', {
                    to : {
                        height : '0'
                    }
                }).then(collapseDone);
            }

            function collapseDone() {

                element.css({
                    height : '0'
                }); // Required so that collapse works when animation is disabled
                element.removeClass('collapsing');
                element.addClass('collapse');
            }

            accordionTable.addDetailRow(scope);
            //            $log.info("data row row" + dataRow.row.isOpen);
            //            scope.$watch('dataRow.row.isOpen', function(shouldCollapse) {
            //
            //                $log.info("shouldCollapse" + shouldCollapse);
            //                if (shouldCollapse) {
            //                    collapse();
            //                } else {
            //                    expand();
            //                }
            //            });
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
