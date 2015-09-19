set('xmttr.loglevel', '3');
use('TXT', 'STDOUT');

var test = 42;
function func() {
    return 'wow1';
}

emit(generate([
    'var1',
    'var2',
    'var3',
    'var4',
    'var5',
    'var6',
    'var7',
    'var8'
].join('\n'), {
    'var1': 'Hello',
    'var2': 'World',
    'var3': '`var1 + \', \' + var2 + \'!\'`',
    'var4': '`var1 == \'Hello\'`?HAHA',
    'var5': '`test`',
    'var6': '`func()`',
    'var7': 'inn(12)',
    'var8': '`inn(10)`'
}));

null;