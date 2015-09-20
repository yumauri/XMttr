# XMttr library

XMttr library contains interfaces and abstract classes for developing modules, emitters and generators. Also contains class **Scribe** for logging purposes.

Each module should extend abstract class *XMttrGenerator* (for generators), or abstract class *XMttrEmitter* (for emitters) and implement interface *XMttrModule*.

Modules can use any external libraries. Libraries should be placed under *lib/* folder, only in that case they will be found and autoloaded.