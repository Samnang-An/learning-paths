import template from './empty-members.html';

import App from 'application';
import * as enumerations from 'models/enumerations';
import ValamisUIControls from 'behaviors/valamis-ui-controls';

const EmptyMembersView = Marionette.View.extend({
    template: template,
    templateContext: function () {
        let membersType = [], addMembers = [];
        _.each(enumerations.memberType, (index, value) => {
            membersType.push({
                value: value,
                label: App.language[value + 'MembersLabel']
            });
            addMembers.push({
                value: value,
                label: App.language[value + 'AddLabel']
            });
        });

        return {
            addMembers: addMembers
        }
    },
    ui: {
        'addMembers': '.js-empty-add-members'
    },
    events: {
        'click @ui.addMembers li': 'addMembers'
    },
    behaviors: [ValamisUIControls],
    addMembers: function (e) {
        let memberType = enumerations.memberType[$(e.target).attr('data-value')];
        this.triggerMethod('add:member', memberType);
    }
});

export default EmptyMembersView