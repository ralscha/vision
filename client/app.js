Ext.application({
    name: 'Vision',

    extend: 'Vision.Application',

    requires: [
        'Vision.view.image.Image'
    ],
    mainView: 'Vision.view.image.Image'
});
