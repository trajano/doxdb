angular.module('doxdbApp', [
    'doxdb', 'ngRoute', 'ngMdIcons'
])

.controller('DoxDBNavController', function(DoxDBService) {

    navController = this;

    navController.schemas = DoxDBService.schemas;
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
