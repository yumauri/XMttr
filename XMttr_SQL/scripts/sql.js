set({
	'xmttr.loglevel': '3',
	'xmttr.logfile': 'sql.log',

    // Chinook database https://chinookdatabase.codeplex.com/
    'xmttr.variables.jdbc.driver': 'com.mysql.jdbc.Driver',
    'xmttr.variables.jdbc.url': 'jdbc:mysql://localhost/Chinook',
    'xmttr.variables.jdbc.user': 'chinook',
    'xmttr.variables.jdbc.password': 'passw0rd',

	'xmttr.template': 'templates/sql.template',
	'xmttr.variables': 'templates/sql.variables',

    // Chinook database https://chinookdatabase.codeplex.com/
    'xmttr.emitter.jdbc.driver': 'com.mysql.jdbc.Driver',
    'xmttr.emitter.jdbc.url': 'jdbc:mysql://localhost/Chinook',
    'xmttr.emitter.jdbc.user': 'chinook',
    'xmttr.emitter.jdbc.password': 'passw0rd',
    'xmttr.emitter.jdbc.simulate': 'false'
});

use('SQL');
init();
emit();

null;
