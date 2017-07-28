import template from './layout.html';

import Sidebar from './sidebar';
import Toolbar from './toolbar';
import Lesson from './goals-content/lesson';
import WebContent from './goals-content/web-content';
import GoalHint from './goals-content/goal-hint';

import App from 'application';
import appConfig from 'config';

import LearningPath from 'models/learning-path';

const PathViewer  = Marionette.View.extend({
    template: template,
    className: 'portlet-wrapper path-viewer',
    regions: {
        goalsSidebar: '.js-goals-sidebar',
        toolbar: '.js-toolbar',
        goalViewer: '.js-goal'
    },
    events: {
        'click .js-done': 'closePathViewer'
    },
    onRender() {
        this.toggleFullscreen(true);

        // create new model just for sidebar for events and so on
        this.path = new LearningPath(this.model.toJSON());

        this.goalsSidebarView = new Sidebar({
            path: this.path,
            goals: this.options.goals
        });
        this.path.on('change:selected', this.onItemSelected, this);
        this.showChildView('goalsSidebar', this.goalsSidebarView);

        this.toolbarView = new Toolbar();
        this.showChildView('toolbar', this.toolbarView);

        this.goalView = new GoalHint({
            goalInfo: App.language['chooseGoalLabel']
        });
        this.showChildView('goalViewer', this.goalView);
    },
    onItemSelected() {
        this.getRegion('goalViewer').reset();

        let item = this.path.get('selected');
        if (item.isLesson() && item.isTincanLesson() && appConfig().isValamisDeployed) {
            this.goalView = new Lesson({
                lessonId: item.get('lessonId')
            });
        }
        else if (item.isWebContent()) {
            this.goalView = new WebContent({
                webContentId: item.get('webContentId')
            });
        }
        else {
            this.goalView = new GoalHint({
                goalInfo: App.language[item.get('goalType') + 'SelectGoalLabel']
            });
        }
        this.showChildView('goalViewer', this.goalView);
    },
    toggleFullscreen(isFullscreen) {
        $('#learningPathsModal .bbm-modal').toggleClass('fullscreen', isFullscreen);
    },
    closePathViewer() {
        this.toggleFullscreen(false);
        this.triggerMethod('viewer:back');
    }
});

export default PathViewer;