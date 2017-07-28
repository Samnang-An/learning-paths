import template from './goals-preview-item.html';
import mainTemplate from './goals-main.html';

import App from 'application';
import Radio from 'backbone.radio';
import * as enumerations from 'models/enumerations';
import DurationUtil from 'utils/duration-utils';

import Goals from 'collections/goals';

const PathGoalItem = Marionette.View.extend({
    tagName: 'li',
    template: template,
    templateContext() {
        let timeLimit = (!!this.model.get('timeLimit'))
            ? DurationUtil.getHumanizeDuration(this.model.get('timeLimit'))
            : '-';

        let childrenNumber = this.model.isGroup() ? this.model.get('goals').length : 0;

        let forUserMember = {};

        if (this.options.isUserMember) {
            let userResult = _.find(this.options.userProgress, {goalId: this.model.get('id')});
            let userGoalStatus = (userResult) ? userResult.status : '';
            forUserMember.isSucceed = userGoalStatus === enumerations.statuses.success;
            forUserMember.isFailed = userGoalStatus === enumerations.statuses.failed;
            forUserMember.isInProgress= !forUserMember.isSucceed && !forUserMember.isFailed
        }

        return _.extend({
            isGroup: this.model.isGroup(),
            isActivity: this.model.isActivity(),
            goalTypeLabel: App.language[this.model.get('goalType')+'GoalLabel'],
            timeLimit: timeLimit,
            isGroupCompletedByCount: this.model.isCompletedByCount(),
            isParentCompletedByCount: this.options.isParentCompletedByCount,
            childrenNumber: childrenNumber,
            isUserMember: this.options.isUserMember
        }, forUserMember);
    },
    regions: {
        tree: {
            el: 'ul.js-group-children',
            replaceElement: true
        }
    },
    onRender() {
        let goals = this.model.get('goals');
        if (goals && goals.length) {
            let groupView = new PathGoalsGroup({
                collection: new Goals(goals),
                userProgress: this.options.userProgress,
                isParentCompletedByCount: this.model.isCompletedByCount(),
                isUserMember: this.options.isUserMember
            });
            groupView.collection.on('bubble:event', (modelTitle) => {
                this.model.trigger('bubble:event', modelTitle);
            });

            this.showChildView('tree', groupView);
        }
    }
});

const PathGoalsGroup = Marionette.CollectionView.extend({
    tagName: 'ul',
    childView: PathGoalItem,
    childViewOptions() {
        return {
            userProgress: this.options.userProgress,
            isParentCompletedByCount: this.options.isParentCompletedByCount,
            isUserMember: this.options.isUserMember
        }
    }
});

const PathGoalsPreview = Marionette.View.extend({
    template: mainTemplate,
    className: 'val-list-table',
    regions: {
        goalsList: '.js-goals-list'
    },
    ui: {
        doneCount: '.js-done-count',
        header: '.js-header'
    },
    templateContext() {
        return {
            isPreview: true,
            goalsCount: this.options.path.get('goalsCount'),
            isMember: this.isUserMember
        }
    },
    initialize() {
        this.isUserMember = !!this.options.userId;
    },
    onRender() {

        this.goals = new Goals();
        this.goals.pathId = this.options.path.get('id');
        this.goals.versionId = this.options.versionId;

        let defGoals = $.Deferred();
        let defProgress = $.Deferred();

        this.goals.fetch({
            getVersion: !this.options.doAsAdmin && this.isUserMember,
            activities: App.activities.toJSON()
        }).then(
            () => {
                this.triggerMethod('goals:loaded', this.goals);
                defGoals.resolve();
            },
            () => { defGoals.reject(); }
        );

        this.userProgress = [];
        if (this.isUserMember) {
            this.options.path.getUserGoalsProgress({}, {userId: this.options.userId}).then(
                (response) => {
                    this.userProgress = response;
                    defProgress.resolve();
                },
                () => { defProgress.reject(); }
            );
        }
        else {
            defProgress.resolve();
        }

        $.when(defGoals, defProgress).then(
            () => { this.renderTree() },
            () => { Radio.channel('notify').trigger('notify', 'error', 'failedLabel'); }
        )
    },
    renderTree(){
        if (this.isUserMember) {
            let doneCount = _.filter(this.userProgress, {status: enumerations.statuses.success}).length;
            this.ui.doneCount.text(doneCount);
        }

        this.ui.header.toggleClass('hidden', this.goals.length === 0);

        let goalsView = new PathGoalsGroup({
            collection: this.goals,
            userProgress: this.userProgress,
            isUserMember: this.isUserMember
        });
        this.showChildView('goalsList', goalsView);
    }
});

export default PathGoalsPreview;