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
}).factory('echoSocket', function($window) {

    var loc = $window.location;
    var wsUri;
    if (loc.protocol === "https:") {
        wsUri = "wss:";
    } else {
        wsUri = "ws:";
    }
    wsUri += '//' + loc.host + loc.pathname.substring(0, loc.pathname.lastIndexOf('/')) + '/doxdb';

    var webSocket = new WebSocket(wsUri);
    $window.onbeforeunload = function() {

        websocket.onclose = function() {

        }; // disable onclose handler first
        websocket.close();
    };
    return webSocket;
}).controller('NotificationsController', function($resource, $scope, echoSocket) {

    var notifications = this;
    notifications.interactions = [];
    echoSocket.onmessage = function(event) {
        notifications.interactions.push({
            message : angular.fromJson(event.data)
        });
        $scope.$apply();
    };

});
