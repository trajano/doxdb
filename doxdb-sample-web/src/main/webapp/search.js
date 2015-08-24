angular.module('search', [
    'ngResource'
]).controller('SearchController', function($resource) {

    var MyIndex = $resource('V1/search/myindex', null, {
        'search' : {
            'method' : 'GET'
        },
        'reindex' : {
            'method' : 'OPTIONS',
            'url' : 'V1/reindex'
        }
    });

    var search = this;

    search.query = '';
    search.result = {};
    search.reindex = function() {

        MyIndex.reindex();
    };

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
