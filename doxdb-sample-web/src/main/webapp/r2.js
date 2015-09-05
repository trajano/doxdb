angular.module('doxdbApp', [
    'doxdb.module', 'ngRoute', 'ngAnimate', 'accordionTable'
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
