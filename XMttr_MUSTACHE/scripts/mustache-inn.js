/**
 * Plugin for XMttr, generates random valid INN numbers (both 10-length and 12-length)
 * Change COUNT variable to change INN count
 * MUSTACHE module required
 *
 * @author Victor Didenko
 * yumaa.verdin@gmail.com
 * 24.08.2015
 */

use('MUSTACHE');

var COUNT = 10;

generate([
    'INN(10)\t\tINN(12)\n',
    '{{#inns}}',
        '{{inn10}}\t{{inn12}}\n',
    '{{/inns}}'
].join(''), {
    'inns': '[' + COUNT,
    'inns.inn10': 'inn(10)',
    'inns.inn12': 'inn(12)'
});
