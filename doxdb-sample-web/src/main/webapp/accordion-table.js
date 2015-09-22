'use strict';
/**
 * <h2>Features</h2>
 * <ul>
 * <li>Works with Material Design Lite and Bootstrap tables.</li>
 * </ul>
 * <h2>Known issues</h2>
 * <ul>
 * <li>Requires <code>ng-repeat</code>. It cannot work correctly with
 * individual entries and will trigger open and close for the entire table.</li>
 * <li>Animations against the <code>tr</code> does not work.</li>
 * </ul>
 */
angular.module('accordionTable', [])

.directive('accordionTable', function() {

    return {
        scope : {},
        controller : [
            '$scope', function($scope) {

                /*
                 * Hack initialization is to get rid of the Eclipse warning
                 * flagging currentDataRowScope is unused.
                 */
                var currentDataRowScope = null;
                currentDataRowScope !== undefined;

                this.addDataRowScope = function(dataRowScope) {

                    currentDataRowScope = dataRowScope;
                };

                this.addDetailRowScope = function(detailRowScope) {

                    detailRowScope.dataRow = currentDataRowScope;
                };

            }
        ]

    };
})

.directive('accordionTableDataRow', function() {

    return {
        require : '^accordionTable',
        controller : [
            '$scope', function($scope) {

                $scope.rowIsExpanded = false;
            }
        ],
        link : function(scope, iElement, iAttrs, accordionTableCtrl) {

            scope.numberOfColumns = iElement.children().length;
            accordionTableCtrl.addDataRowScope(scope);
            iElement.on('click', function(event) {

                scope.$apply(function() {

                    scope.rowIsExpanded = !scope.rowIsExpanded;
                });
            });
        }
    };
})

.directive('accordionTableDetailRow', function() {

    return {
        scope : {},
        require : '^accordionTable',
        link : function(scope, element, iAttrs, accordionTable) {

            accordionTable.addDetailRowScope(scope);
            // Correct the colspan regardless whether it is set or not.
            angular.element(element.children()[0]).attr("colspan", scope.dataRow.numberOfColumns);

            scope.$watch('dataRow.rowIsExpanded', function(rowIsExpanded) {

                if (rowIsExpanded) {
                    element.removeClass('ng-hide');
                } else {
                    element.addClass('ng-hide');
                }
            });

        }
    };
});
