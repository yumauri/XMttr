set({
    'xmttr.loglevel': '2',
    'xmttr.logfile': 'jms.log',

	'xmttr.template': 'templates/jms.template',
	'xmttr.variables': 'templates/jms.variables',
	'date.format': 'MM/dd/yyyy',

    // Chinook database https://chinookdatabase.codeplex.com/
    'xmttr.variables.jdbc.driver': 'org.sqlite.JDBC',
    'xmttr.variables.jdbc.url': 'jdbc:sqlite:Chinook_Sqlite.sqlite',

    // jms client

    // weblogic
    'xmttr.emitter.factory': 'weblogic.jndi.WLInitialContextFactory',
    'xmttr.emitter.url': 't3://localhost:7001',
    //'xmttr.emitter.principal': 'CHANGE_ME',
    //'xmttr.emitter.credentials': 'CHANGE_ME',

    // WebSphere
    //'xmttr.emitter.factory': 'com.sun.jndi.fscontext.RefFSContextFactory',
    //'xmttr.emitter.url': 'file:lib', // directory with .bindings file

    'xmttr.emitter.connection': 'connectionFactory1',
    'xmttr.emitter.queue': 'queue1',

    // 'xmttr.emitter.skipsend': 'yes',
    'count': '10',
    'observe': 'yes'
});

use('HTTL', 'JMS');
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
 * Queue observer
 */
function Observer(heap) {
    var TIMEOUT_BEFORE_CHECK = 500, // ms between checks
        ERROR_COUNT = 3; // errors, before quit
    log(3, 'Observer created');
    return {
        start: function() {
            startTimer('Observer');
            log('Observer begin to observe');

            var isQueueEmpty = false,
                errorCount = 0;

            // wait until at least one message was sent
            while (heap.get() === XML_COUNT) {
                sleep(TIMEOUT_BEFORE_CHECK);
            }

            // start observing
            do {
                sleep(TIMEOUT_BEFORE_CHECK); // wait a bit

                // check if queue is empty
                try {
                    isQueueEmpty = xMttr.emitter.isQueueEmpty();
                } catch (e) {
                    log('Observer cannot observe queue: ' + e);
                    errorCount++;
                }
            } while (!isQueueEmpty && errorCount <= ERROR_COUNT);

            if (errorCount > ERROR_COUNT) {
                log('Observer has finished to observe due to many errors...');
                return;
            }

            var diff = stopTimer('Observer'),
                speed = Math.round( (60000 * XML_COUNT / diff) * 100 ) / 100; // 60000 milliseconds in 1 second
            log('Observer has finished to observe, all ' + XML_COUNT + ' messages got their aims in ' + diff + 'ms');
            log('That would be approximately ' + speed + ' messages per minute');
        }
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

    // start observer
    var observe = ('' + get('observe')).toLowerCase();
    if ('yes' === observe || 'true' === observe || '1' === observe) {
        log(3, 'Create observer to observe messages in the queue');
        var observer = new Observer(heap);
        spawn(observer.start); // start in new thread
    } else {
        log(3, 'Skip observer, "observe" is "no"...');
    }

    // start all trebuchets
    log(3, "Create and start trebuchets");
    for (var i = 0; i < TREBUCHET_COUNT; i++) {
        var trebuchet = new Trebuchet(i + 1, heap);
        spawn(trebuchet.start); // start in new thread
    }

})();