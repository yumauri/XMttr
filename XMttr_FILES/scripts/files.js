set({
    'xmttr.loglevel': '2',
    'xmttr.logfile': 'files.log',

	'xmttr.template': 'templates/files.template',
	'xmttr.variables': 'templates/files.variables',
	'date.format': 'MM/dd/yyyy',

    // Chinook database https://chinookdatabase.codeplex.com/
    'xmttr.variables.jdbc.driver': 'org.sqlite.JDBC',
    'xmttr.variables.jdbc.url': 'jdbc:sqlite:Chinook_Sqlite.sqlite',

    // files emitter
    'xmttr.emitter.folder': 'files-emitter',
    'xmttr.emitter.file': 'xmttr-files-%2$tY%2$tm%2$td-%2$tH%2$tM%2$tS-%2$tL-%1$05d.xml',

    'count': '10'
});

use('HTTL', 'FILES');
init(); // preload variables

var TREBUCHET_COUNT = 10,
    XML_COUNT = +get('count');

/**
 * Message heap
 */
function MsgHeap(msgCount) {
    var messagesLeft = msgCount;
    return {
        get: function() {
            return messagesLeft;
        },
        getAndDecrement: sync(function() {
            return messagesLeft--;
        })
    }
}

/**
 * Messages trebuchet
 */
function Trebuchet(number, heap) {
    var n = number;
    log(3, 'Trebuchet ' + n + ' created');
    return {
        start: function() {
            startTimer('Trebuchet' + n);
            log('Trebuchet ' + n + ' launched');

            var sent = 0,
                messagesLeft = heap.getAndDecrement();
            while (messagesLeft > 0) {
                sent++;
                try {
                    startTimer('GenerateMessageTrebuchet' + n);
                    var messageNumber = XML_COUNT - messagesLeft + 1;
                    var message = generate({
                        'MessageNumber': '' + messageNumber,
                        'Trebuchet': '' + n
                    });
                    log(1, 'Trebuchet ' + n + ' generated message ' + messageNumber + ' in ' + stopTimer('GenerateMessageTrebuchet' + n) + 'ms');

                    emit(message);
                    log(2, 'Trebuchet ' + n + ' hurled message ' + messageNumber);
                } catch (e) {
                    log('Trebuchet ' + n + ' cannot hurl message: ' + e);
                }
                messagesLeft = heap.getAndDecrement();
            }

            log('Trebuchet ' + n + ' out of messages, hurled ' + sent + ' ones in ' + stopTimer('Trebuchet' + n) + 'ms');
        }
    }
}

/**
 * Entry point
 */
(function() {

    // check count of messages
    if (!XML_COUNT) {
        log('Message count is 0, define "count" property');
        return;
    }

    // if count of messages < count of trebuchets - there is no need in unnecessary trebuchets
    if (XML_COUNT < TREBUCHET_COUNT) {
        TREBUCHET_COUNT = XML_COUNT;
    }

    // set message heap
    var heap = new MsgHeap(XML_COUNT);

    // start all trebuchets
    log(3, "Create and start trebuchets");
    for (var i = 0; i < TREBUCHET_COUNT; i++) {
        var trebuchet = new Trebuchet(i + 1, heap);
        spawn(trebuchet.start); // start in new thread
    }

})();