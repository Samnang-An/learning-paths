import template from './toolbar.html';

import ValamisUIControls from 'behaviors/valamis-ui-controls';
import LiferayUtil from 'utils/liferay-utils';
import * as enumerations from 'models/enumerations';

const ToolbarLayout = Marionette.View.extend({
    className: 'val-tabs',
    template : template,
    behaviors: [ValamisUIControls],
    templateContext: function() {
        return {
            isList : this.isList(),
            isTiles : this.isTiles(),
            currentCourseId: LiferayUtil.courseId(),
            groups: enumerations.pathsGroups
        };
    },
    ui: {
        addNew: '.js-add-new',
        displayMode: '.js-display-option',
        searchField: '.js-search input',
        scope: '.js-scope-filter',
        sort: '.js-sort-filter',
        group: '.js-paths-groups a'
    },
    events: {
        'click @ui.addNew': 'addNew',
        'click @ui.displayMode':  'toggleDisplayMode',
        'keyup @ui.searchField': 'changeSearchText',
        'click @ui.scope li': 'changeScope',
        'click @ui.sort li': 'changeSort',
        'click @ui.group': 'changeGroup'
    },
    onRender() {
        this.on('valamis:controls:init', () => {
            this.ui.sort.valamisDropDown('select', this.model.get('sort'));
        });
    },
    isTiles() {
        return this.model.get('displayMode') == 'tiles';
    },
    isList() {
        return this.model.get('displayMode') == 'list';
    },
    addNew() {
        this.triggerMethod('open:modal');
    },
    changeSearchText(e) {
        clearTimeout(this.inputTimeout);
        this.inputTimeout = setTimeout(() => {
            this.model.set('title', $(e.target).val());
        }, 800);
    },
    changeScope(e) {
        this.model.set('courseId', $(e.target).attr('data-value'));
    },
    changeSort(e) {
        this.model.set('sort', $(e.target).attr('data-value'));
    },
    changeGroup(e) {
        let filterValues = {
            userId: undefined,
            joined: undefined
        };

        let group = $(e.target).data('value');
        this.ui.group.parent('li').removeClass('active');
        $(e.target).parent('li').addClass('active');

        switch (group) {
            case enumerations.pathsGroups.creator:
                filterValues.userId = LiferayUtil.userId();
                break;
            case enumerations.pathsGroups.member:
                filterValues.joined = true;
                break;
            case enumerations.pathsGroups.notmember:
                filterValues.joined = false;
                break;
        }

        this.model.set(filterValues);
    },
    toggleDisplayMode(evt) {
        evt.preventDefault();
        evt.stopPropagation();
        let $target = $(evt.target).closest('button[data-display-mode]');
        this.ui.displayMode.removeClass('active');
        $target.addClass('active');
        let mode = $target.data('displayMode');
        this.model.set('displayMode', mode);
    }
});

export default ToolbarLayout;
