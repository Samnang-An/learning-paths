import template from './info.html';

import App from 'application';
import Radio from 'backbone.radio';
import DurationUtil from 'utils/duration-utils';

import Course from 'models/course';
import * as enumerations from 'models/enumerations';

import PathGoalsPreview from '../goals-list/goals-preview';
import LiferayUtil from 'utils/liferay-utils';

import Competencies from 'collections/competencies';
import CompetenciesView from './competencies';

const PathInfoPreview  = Marionette.View.extend({
    className: 'preview-wrapper',
    regions: {
        recommendationList: '.js-recommendation-list',
        improvingList: '.js-improving-list',
        goals: '.js-goals-list'
    },
    ui: {
        goalsSubheader: '.js-goals-subheader',
        scope: '.js-scope-title',
        join: '.js-join',
        leave: '.js-leave',
        startPath: '.js-start',
        earnBadge: '.js-earn-badge',
        recommendationSkills: '.js-recommendation-skills',
        improvingSkills: '.js-improving-skills'
    },
    events: {
        'click @ui.join': 'joinPath',
        'click @ui.leave': 'leavePath',
        'click @ui.startPath': 'startPath',
        'click @ui.earnBadge': 'earnBadge'
    },
    template: template,
    templateContext() {
        let validPeriod = this.model.get('validPeriod');
        let validDuration = (!!validPeriod)
            ? DurationUtil.getHumanizeDuration(validPeriod)
            : '';

        let expiringPeriod = this.model.get('expiringPeriod');
        let expiringDuration = (!!expiringPeriod)
            ? DurationUtil.getHumanizeDuration(expiringPeriod)
            : '';

        let statuses = enumerations.statuses;
        let status = this.model.isMember() ? this.model.getStatus() : '';

        return {
            validDuration: validDuration,
            expiringDuration: expiringDuration,
            fullLogoUrl: this.model.getFullLogoUrl(),
            isMember: this.model.isMember(),
            isInProgress: status === statuses.inprogress,
            isSucceed: status === statuses.success,
            isFailed: status === statuses.failed,
            isExpired: status === statuses.expired,
            isExpiring: status === statuses.expiring,
            canEarnBadge: this.model.canEarnBadge()
        }
    },
    onRender() {
        this.goals = undefined;

        if (this.model.get('courseId')) {
            let course = new Course({id: this.model.get('courseId')});
            course.fetch().then(
                () => { this.ui.scope.text(course.get('title')) },
                () => { Radio.channel('notify').trigger('notify', 'error', 'failedLabel'); }
            );
        }
        else {
            this.ui.scope.text(App.language['instanceScopeLabel'])
        }

        let getForVersion = !this.options.doAsAdmin && this.model.isMember();

        let userId = (getForVersion) ? LiferayUtil.userId() : '';

        let goalsView = new PathGoalsPreview({
            path: this.model,
            userId: userId,
            doAsAdmin: this.options.doAsAdmin,
            versionId: this.model.get('statusVersionId')
        });
        goalsView.on('goals:loaded', this.toggleStart, this);
        this.showChildView('goals', goalsView);

        // this.recommendations = new Competencies([], {
        //     getForVersion: getForVersion
        // });
        // this.recommendations.pathId = (getForVersion) ? this.model.get('statusVersionId')
        //     : this.model.get('id');
        // this.recommendations.fetch().then(() => {
        //     if(this.recommendations.length) {
        //         this.ui.recommendationSkills.toggleClass('hidden');
        //          this.showChildView('recommendationList', new CompetenciesView({
        //              collection: this.recommendations
        //         }));
        //     }
        // });
        //
        // this.improvements = new Competencies([], {
        //    getForVersion: getForVersion
        // });
        // this.improvements.pathId = (getForVersion) ? this.model.get('statusVersionId')
        //     : this.model.get('id');
        // this.improvements.fetch({
        //     improving: true
        // }).then(() => {
        //     if(this.improvements.length) {
        //         this.ui.improvingSkills.toggleClass('hidden');
        //         // this.showChildView('improvingList', new CompetenciesView({
        //         //     collection: this.improvements
        //         // }));
        //     }
        // });
    },
    toggleStart(goals) {
        this.goals = goals;
        let showStart = goals.length > 0 && this.model.isMember()
            && this.model.getStatus() === enumerations.statuses.inprogress;
        this.ui.startPath.toggleClass('hidden', !showStart);
    },
    joinPath() {
        this.model.join().then(
            () => { this.model.trigger('user:joined');  },
            () => { this.notifyChannel.trigger('notify', 'error', 'failedLabel'); }
        );
    },
    leavePath() {
        this.model.leave().then(
            () => {
                this.model.unset('status');
                this.model.trigger('user:left');
            },
            () => { this.notifyChannel.trigger('notify', 'error', 'failedLabel'); }
        );
    },
    startPath() {
        if (this.goals) {
            this.model.trigger('open:viewer', this.goals);
        }
        else {
            this.notifyChannel.trigger('notify', 'error', 'failedLabel');
        }
    },
    earnBadge() {
        if (typeof OpenBadges !== undefined) {
            OpenBadges.issue([ this.model.getIssueBadgeUrl() ]);
        }
        else {
            this.notifyChannel.trigger('notify', 'error', 'openBadgesUndefinedLabel');
        }
    }
});

export default PathInfoPreview;