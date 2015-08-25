angular.module('doxdbApp', [
    'ngResource', 'search', 'schemaForm'
]).controller('DoxDbController', function($resource) {

    var Venue = $resource('V1/venue/:id?v=:version', {
        id : '@_id',
        version : '@_version'
    });

    var doxdb = this;

    doxdb.schema = $resource('V1/schema/venue.json').get();

    Venue.query({}, function(venues) {

        doxdb.venues = venues;
        if (venues.length > 0) {
            doxdb.venue = venues[0];
        }
    });

    doxdb.newVenue = function() {

        doxdb.venue = {
            "rings" : []
        };
    };

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
                doxdb.newVenue();
            });
        }
    };

    doxdb.form = [
        "*", {
            type : "button",
            title : "Save",
            style : "btn-primary",
            onClick : doxdb.saveVenue
        }, {
            type : "button",
            title : "Delete",
            style : "btn-danger",
            onClick : doxdb.deleteVenue
        }, {
            type : "button",
            title : "New",
            style : "btn-default",
            onClick : doxdb.newVenue
        }

    ];

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

    	webSocket.onclose = function() {

        }; // disable onclose handler first
        webSocket.close();
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
