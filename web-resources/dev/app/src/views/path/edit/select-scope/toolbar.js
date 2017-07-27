import template from './toolbar.html';

import ValamisUIControls from 'behaviors/valamis-ui-controls';

const ToolbarLayout = Marionette.View.extend({
    template : template,
    ui: {
        'search': '.js-search input',
        'sort': '.js-sort'
    },
    events: {
        'keyup @ui.search': 'changeSearchText',
        'click @ui.sort li': 'changeSort'
    },
    behaviors: [ValamisUIControls],
    onRender() {
        this.on('valamis:controls:init', function() {
            this.ui.sort.valamisDropDown('select', this.model.get('sort'));
        }, this);
    },
    changeSearchText: function(e) {
        clearTimeout(this.inputTimeout);
        this.inputTimeout = setTimeout(() => {
            this.model.set('filter', $(e.target).val());
        }, 800);
    },
    changeSort: function(e){
        this.model.set('sort', $(e.target).attr('data-value'));
    }
});

export default ToolbarLayout;
