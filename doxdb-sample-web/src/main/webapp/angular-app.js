angular.module('todoApp', [
    'ngResource'
]).controller('TodoListController', function($resource) {

    var Venue = $resource('V1/venue/:id?v=:version', {
        id : '@_id',
        version : '@_version'
    });

    var todoList = this;
    function reloadVenues() {

        Venue.query({}, function(venues) {

            todoList.venues = venues;
            if (venues.length > 0) {
                todoList.venue = venues[0];
            }
        });
    }
    reloadVenues();
    todoList.saveVenue = function() {

        if (!todoList.venue._id) {

            new Venue(todoList.venue).$save().then(reloadVenues);
        } else {
            todoList.venue.$save().then(function(venue) {

                todoList.venue = venue;
            });
        }
    };
    todoList.deleteVenue = function() {

        if (todoList.venue._id) {

            var toBeDeleted = todoList.venue;
            toBeDeleted.$delete().then(function(venue) {

                todoList.venues.splice(todoList.venues.indexOf(toBeDeleted), 1);
                todoList.venue = null;
            });
        }
    };
});
