set({
	'xmttr.loglevel': '0',
	'xmttr.template': 'templates/txt.template',
	'xmttr.variables': 'templates/txt.variables'
});

use('TXT', 'STDOUT');
init();

var text = generate();
emit(text);

null;
