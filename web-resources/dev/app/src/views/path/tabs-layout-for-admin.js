
import template from './tabs-layout.html';

import LearningPath from 'models/learning-path';

import SelectGoalLayout from './select-goal/layout';

import PathPreview from './preview/layout';
import PathEdit from './edit/layout';
import PathViewer from './viewer/layout';

import Members from 'collections/members';
import AvailableMembers from 'collections/available-members';
import MembersView from './members/members';

import UserProgress from './user-progress/user-progress';


const TabsLayoutForAdmin  = Marionette.View.extend({
    template: template,
    regions: {
        info: '#pathInfo',
        members: '#pathMembers',
        layout: '#extLayout'
    },
    ui: {
        pathTabs: '.js-valamis-tabs',
        membersTab: '[data-ref="pathMembers"]'
    },
    childViewEvents: {
        'edit:path': 'showEdit',
        'preview:path': 'fetchAndOpenPreview',
        'select:members': 'selectMembers',
        'select:goals:step2': 'selectGoals',
        'viewer:back': 'backFromViewer',
        'select:goals:back': 'backFromSelectGoals',
        'select:members:back': 'backFromSelectMembers',
        'user:progress:back': 'backFromUserProgress'
    },
    modelEvents: {
        'destroy': 'onPathDeleted',
        'open:viewer': 'showViewer',
        'user:joined': 'fetchAndOpenPreview',
        'user:left': 'fetchAndOpenPreview',
    },
    initialize() {
        if (!this.model) {
            this.model = new LearningPath();
        }
        this.doAsAdmin = this.options.doAsAdmin;
    },
    onRender() {
        if (this.model.isNew()) {
            this.showEdit();
            this.ui.membersTab.addClass('disabled');
        }
        else {
            this.showPreview();
            this.showMembers();
        }

        this.ui.pathTabs.valamisTabs();
    },
    onPathDeleted() {
        this.triggerMethod('modal:close');
    },
    // preview mode functions
    showPreview(reloadModel) {
        this.showChildView('info', new PathPreview({
            model: this.model,
            reloadModel: reloadModel,
            doAsAdmin: this.doAsAdmin
        }));
    },
    fetchAndOpenPreview() {
        this.showPreview(true);
        this.showMembers();
    },
    // edit mode functions
    showEdit() {
        let pathEdit = new PathEdit({
            pathModel: this.model
        });
        pathEdit.on('new:path:saved', this.showForNewSavedPath, this);
        this.showChildView('info', pathEdit);
    },
    showForNewSavedPath(newModelData) {
        this.model.set(newModelData);
        this.ui.membersTab.removeClass('disabled');
        this.showMembers();
    },

    // members
    showMembers() {
        this.members = new Members();
        this.members.on('show:user:progress', this.showUserProgress, this);
        this.members.pathId = this.model.get('id');
        let membersList = new MembersView({
            members: this.members
        });
        this.showChildView('members', membersList);
    },
    showUserProgress(userIndex) {
        this.ui.pathTabs.addClass('hidden');

        let userProgress = new UserProgress({
            path: this.model,
            members: this.members,
            userIndex: userIndex
        });
        this.showChildView('layout', userProgress);
    },
    backFromUserProgress() {
        this.ui.pathTabs.removeClass('hidden');
        this.getRegion('layout').empty();
    },

    // members
    selectMembers(memberType) {
        this.ui.pathTabs.addClass('hidden');

        this.members = new AvailableMembers();
        this.members.pathId = this.model.get('id');
        let membersList = new MembersView({
            members: this.members,
            memberType: memberType,
            available: true,
            path: this.model
        });
        this.showChildView('layout', membersList);
    },
    backFromSelectMembers() {
        this.showMembers();
        this.ui.pathTabs.removeClass('hidden');
        this.getRegion('layout').empty();
        this.showPreview(true);
    },

    // goals
    selectGoals(goalType, draftModel) {
        this.ui.pathTabs.addClass('hidden');

        this.showChildView('layout', new SelectGoalLayout({
            goalType: goalType,
            pathDraft: draftModel
        }));
    },
    backFromSelectGoals() {
        this.showEdit();
        this.ui.pathTabs.removeClass('hidden');
        this.getRegion('layout').empty();
    },

    // viewer
    showViewer(goals) {
        this.ui.pathTabs.addClass('hidden');

        this.showChildView('layout', new PathViewer({
            model: this.model,
            goals: goals
        }));
    },
    backFromViewer() {
        this.showPreview(true);
        this.ui.pathTabs.removeClass('hidden');
        this.getRegion('layout').empty();
    }

});

export default TabsLayoutForAdmin;