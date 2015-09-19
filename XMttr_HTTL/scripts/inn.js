/**
 * Plugin for XMttr, generates random valid INN numbers (both 10-length and 12-length)
 * Change COUNT variable to change INN count
 * HTTL module required
 *
 * @author Victor Didenko
 * yumaa.verdin@gmail.com
 * 27.10.2014
 */

set('default.variable.type', 'java.lang.Object');
use('HTTL');

var COUNT = 10;

generate([
    'INN(10)\t\tINN(12)\n',
    '<!--#for(Map<String,String> i : inns)-->',
        '${i.inn10}\t${i.inn12}\n',
    '<!--#end-->'
].join(''), {
    'inns': '[' + COUNT,
    'inns.inn10': 'inn(10)',
    'inns.inn12': 'inn(12)'
});
