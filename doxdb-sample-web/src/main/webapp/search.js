angular.module('search', [
    'ngResource'
]).controller('SearchController', function($resource) {

    var MyIndex = $resource('V1/search/myindex', null, {
        'search' : {
            'method' : 'GET'
        }
    });

    var search = this;

    search.query = '';
    search.result = {};
    search.doSearch = function() {

        if (search.query) {
            MyIndex.search({
                'q' : search.query
            }, function(result) {

                search.result = result;
            });
        }
    };

});
