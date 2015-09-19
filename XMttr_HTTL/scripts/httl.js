set({
	'xmttr.loglevel': '3',

	'xmttr.template': 'templates/httl.template',
	'xmttr.variables': 'templates/httl.variables',
	'date.format': 'MM/dd/yyyy',

    // Chinook database https://chinookdatabase.codeplex.com/
    'xmttr.variables.jdbc.driver': 'org.sqlite.JDBC',
    'xmttr.variables.jdbc.url': 'jdbc:sqlite:Chinook_Sqlite.sqlite',

	'xmttr.emitter.file': 'httl.out',
	'xmttr.emitter.append': 'true'
});

use('HTTL', 'FILE');
init();

var text = generate();
emit(text);

null;
