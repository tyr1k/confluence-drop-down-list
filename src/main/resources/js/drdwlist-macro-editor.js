AJS.toInit(function() {
    var macroParams = {};
    if (window.MacroDialog && MacroDialog.getMacroParameters) {
        macroParams = MacroDialog.getMacroParameters();
    }
    if (!macroParams.propertyKey) {
        macroParams.propertyKey = 'drdwlist-' + generateUUID();
    }
    document.getElementById('label').value = macroParams.label || '';
    document.getElementById('default').value = macroParams.default || '';
    document.getElementById('values').value = macroParams.values || 'dev,stage,prod';

    document.getElementById('macro-form').onsubmit = function(e) {
        e.preventDefault();
        MacroDialog.insertMacro({
            propertyKey: macroParams.propertyKey,
            label: document.getElementById('label').value,
            default: document.getElementById('default').value,
            values: document.getElementById('values').value
        });
        return false;
    };

    function generateUUID() {
        return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
            var r = Math.random()*16|0,v=c=='x'?r:(r&0x3|0x8);
            return v.toString(16);
        });
    }
});


