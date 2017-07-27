import template from './sidebar.html';
import goalTemplate from './sidebar-item.html';

import Goals from 'collections/goals';

import LiferayUtil from 'utils/liferay-utils';

const GoalItemView = Marionette.View.extend({
    tagName: 'li',
    className: 'sidebar-item js-sidebar-item',
    template: goalTemplate,
    ui: {
        'item': '> .js-item',
        'childContainer': '> .js-child-container'
    },
    templateContext() {
        return {
            isGroup: this.model.isGroup()
        }
    },
    regions: {
        tree: {
            el: '> .js-child-container > ul',
            replaceElement: true
        }
    },
    events: {
        'click @ui.item': 'onItemClick'
    },
    modelEvents: {
        'change:active': 'toggleActive'
    },
    onRender() {
        this.options.path.on('change:selected', () => {
            let isActive = this.model.get('id') == this.options.path.get('selected').get('id');
            this.model.set('active', isActive);
        });
        this.options.path.on('change:progress', () => {
            let goalProgress = _.find(this.options.path.get('progress'), {goalId: this.model.get('id')});
            if (goalProgress) {
                if (goalProgress.status !== 'InProgress') {
                    this.ui.item.addClass(goalProgress.status.toLowerCase())
                }
            }
        });

        let goals = this.model.get('goals');
        if (goals && goals.length) {
            let listView = new SidebarListView({
                collection: new Goals(goals),
                path: this.options.path
            });
            this.showChildView('tree', listView);
        }
    },
    onItemClick() {
        if (this.model.isGroup()) {
            this.toggleListView();
        }
        else {
            this.selectItem();
        }
    },
    toggleListView() {
        this.ui.item.toggleClass('val-icon-plus');
        this.ui.item.toggleClass('val-icon-minus');
        this.ui.childContainer.toggleClass('hidden');
    },
    selectItem() {
        this.options.path.set('selected', this.model);
    },
    toggleActive() {
        this.ui.item.toggleClass('active', this.model.get('active'));
    }
});

const SidebarListView = Marionette.CollectionView.extend({
    tagName: 'ul',
    className: 'sidebar-list',
    childView: GoalItemView,
    childViewOptions() {
        return { path: this.options.path }
    }
});

const Sidebar = Marionette.View.extend({
    template: template,
    className: 'sidebar',
    regions: {
        goalsList: '.js-goals-list '
    },
    templateContext() {
        return {
            title: this.options.path.get('title')
        }
    },
    ui: {
        progressLabel: '.js-progress-label',
        progress: '.js-progress'
    },
    initialize() {
        this.goals = new Goals(this.options.goals.toJSON());
        this.options.path.on('change:selected', this.selectGoal, this);
    },
    onRender() {
        let sidebarListView = new SidebarListView({
            collection: this.goals,
            path: this.options.path
        });
        this.showChildView('goalsList', sidebarListView);

        this.getUserProgress();
    },
    selectGoal() {
        // refresh the tree and progress in 1 sec
        clearTimeout(this.refreshTimeout);
        this.refreshTimeout = setTimeout(() => {
            this.getUserProgress();
        }, 1000);
    },
    getUserProgress() {
        this.options.path.getUserGoalsProgress({}, {
            userId: LiferayUtil.userId()
        }).then(
            (progress) => {
                this.options.path.set('progress', progress);
            }
        );

        this.options.path.getUserProgress({}, {
            userId: LiferayUtil.userId()
        }).then(
            (response) => {
                this.setUserProgress(response.progress)
            }
        );
    },
    setUserProgress(progress) {
        let progressPercent = Math.round((progress || 0) * 100) + '%';
        this.ui.progressLabel.text(progressPercent);
        this.ui.progress.css('width', progressPercent);
    }
});

export default Sidebar;