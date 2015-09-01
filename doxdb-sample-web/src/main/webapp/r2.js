angular.module('doxdbApp', [
    'doxdb', 'ngRoute'
])

.config(function($routeProvider) {

    $routeProvider

    .when("/welcome", {
        templateUrl : "partials/welcome.html",
        controller : "DoxDBWelcomeController",
        controllerAs : "doxdbWelcome"
    })

    .when("/:name", {
        templateUrl : "partials/schema.html",
        controller : "DoxDBSchemaController",
        controllerAs : "doxdbSchema"
    })

    .otherwise("/welcome");

})

.controller('DoxDBNavController', function(DoxDBService) {

    navController = this;

    navController.schemas = DoxDBService.schemas;
})

.controller('DoxDBSchemaController', function(DoxDBService, $routeParams, $log) {

    schemaController = this;
    $log.info($routeParams);
    $log.info("HERE");
    schemaController.schema = $routeParams.name;
})

.controller('DoxDBWelcomeController', function(DoxDBService) {

    welcomeController = this;

})

.service('DoxDBService', function() {

    this.schemas = [
        {
            "name" : "Venue",
            "schema" : "venue"
        }, {
            "name" : "Horse",
            "schema" : "horse"
        }
    ];

});
