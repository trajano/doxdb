angular.module('todoApp', [
    'ngResource'
]).controller('TodoListController', function($resource) {

    var Venue = $resource('/doxdb/V1/venue/:id', {
        id : '@_id'
    });

    var todoList = this;
    Venue.query({}, function(venues) {

        todoList.venue = venues[0];
    });

    todoList.addTodo = function() {

        todoList.venue.$save().then(function(venue) {

            todoList.venue = venue;
        });
    };
});
