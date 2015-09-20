set({
	'xmttr.template': 'templates/mustache-partials1.template'
});

use('MUSTACHE', 'STDOUT');

var text = generate({
	'name': 'World',
	'what': 'day',
	'now': 'now()'
});
emit(text);

null;
