angular.module('todoApp', [
    'ngResource'
]).controller('TodoListController', function($resource) {

    var Venue = $resource('/doxdb/V1/venue/:id', {
        id : '@_id'
    });

    var todoList = this;
    //    Restangular.oneUrl('/doxdb/V1/venue', 'wtKEgZb1TuvVfibLwJwt4q0Gpzqre3KP').getList().then(function(venues) {
    //
    //        todoList.venue = venues[0];
    //    });

    Venue.get({
        id : 'wtKEgZb1TuvVfibLwJwt4q0Gpzqre3KP'
    }, function(venue) {

        todoList.venue = venue;
    });
    //
    todoList.addTodo = function() {

        todoList.venue.$save().then(function(venue) {

            todoList.venue = venue;
        });
    };
});
