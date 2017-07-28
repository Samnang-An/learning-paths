import toolbarTemplate from './toolbar.html';

const Toolbar = Marionette.View.extend({
    template: toolbarTemplate,
    className: 'path-toolbar'
});

export default Toolbar;