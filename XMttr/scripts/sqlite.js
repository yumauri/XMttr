set({
	// 'xmttr.loglevel': '3',

	'xmttr.template': 'templates/sqlite.template',
	'xmttr.variables': 'templates/sqlite.variables',

    // Chinook database https://chinookdatabase.codeplex.com/
    'xmttr.variables.jdbc.driver': 'org.sqlite.JDBC',
    'xmttr.variables.jdbc.url': 'jdbc:sqlite:Chinook_Sqlite.sqlite'
});

use('TXT', 'STDOUT');
init();
emit(generate());

null;
