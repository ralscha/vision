Ext.application({
    extend: 'Vision.Application',

    name: 'Vision',

    requires: [
        'Vision.*'
    ],
    mainView: 'Vision.view.image.Image'
});
