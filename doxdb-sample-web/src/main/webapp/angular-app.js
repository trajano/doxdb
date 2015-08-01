angular.module('doxdbApp', [
    'ngResource', 'search'
]).controller('DoxDbController', function($resource) {

    var Venue = $resource('V1/venue/:id?v=:version', {
        id : '@_id',
        version : '@_version'
    });

    var doxdb = this;

    Venue.query({}, function(venues) {

        doxdb.venues = venues;
        if (venues.length > 0) {
            doxdb.venue = venues[0];
        }
    });

    doxdb.saveVenue = function() {

        if (!doxdb.venue._id) {

            new Venue(doxdb.venue).$save().then(function(venue) {

                doxdb.venue = venue;
                doxdb.venues.push(venue);
            });
        } else {
            doxdb.venue.$save().then(function(venue) {

                doxdb.venue = venue;
            });
        }
    };
    doxdb.deleteVenue = function() {

        if (doxdb.venue._id) {

            var toBeDeleted = doxdb.venue;
            toBeDeleted.$delete().then(function(venue) {

                doxdb.venues.splice(doxdb.venues.indexOf(toBeDeleted), 1);
                doxdb.venue = null;
            });
        }
    };
});
