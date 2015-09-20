set({
    'xmttr.loglevel': '3',

    'xmttr.template': 'templates/mustache.template',
    'xmttr.variables': 'templates/mustache.variables',
    'date.format': 'MM/dd/yyyy',

    // Chinook database https://chinookdatabase.codeplex.com/
    'xmttr.variables.jdbc.driver': 'org.sqlite.JDBC',
    'xmttr.variables.jdbc.url': 'jdbc:sqlite:Chinook_Sqlite.sqlite',

	'xmttr.emitter.file': 'mustache.out',
	'xmttr.emitter.append': 'true'
});

use('MUSTACHE', 'FILE');
init();

var text = generate();
emit(text);

null;
