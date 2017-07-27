import ValamisModel from 'models/valamis';
import appConfig from 'config';
import LiferayUtil from 'utils/liferay-utils';
import moment from 'moment';

import * as enumerations from 'models/enumerations';

const LearningPathService = new Backbone.Service({
    url: function (model) {
        return appConfig().endpoint.learningPaths
    },
    targets: {
        'join': {
            'path': 'join',
            'method': 'post'
        },
        'leave': {
            'path': 'leave',
            'method': 'post'
        },
        'clone': {
            'path': 'clone',
            'method': 'post'
        },
        'publish': {
            // we can publish only draft version
            'path': 'draft/publish',
            'method': 'post'
        },
        'activate': {
            'path': 'activate',
            'method': 'post'
        },
        'deactivate': {
            'path': 'deactivate',
            'method': 'post'
        },
        'addMembers': {
            path: (model, options) => {
                return 'members/' + options.memberType
            },
            data: (model, options) => {
                return options.selectedIds
            },
            method: 'post'
        },
        'createDraft': {
            'path': 'draft',
            'method': 'post'
        },
        'getUserGoalsProgress': {
            'path': (model , options) => {
                return 'members/users/' + options.userId + '/goals-progress'
            },
            'method': 'get'
        },
        'getUserProgress': {
            'path': (model , options) => {
                return 'members/users/' + options.userId + '/progress'
            },
            'method': 'get'
        }
    }
});

const LearningPath = ValamisModel.extend({
    getFullLogoUrl() {
        let logoUrl = this.get('logoUrl');
        return (logoUrl) ? `${appConfig().apiPath}${logoUrl}` : '';
    },
    isMember() {
        return this.get('published') && (this.get('status') !== undefined)
    },
    canEarnBadge() {
        return this.get('status') == enumerations.statuses.success && this.get('openBadgesEnabled');
    },
    getIssueBadgeUrl() {
        return 'http://' + appConfig().root + appConfig().apiPath
            + 'certificates/' + this.get('id')
            + '/issue_badge?userId=' + LiferayUtil.userId()
            + '&rootUrl=' + appConfig().root
    },
    getStatus() {
        let status = this.get('status');
        let statuses = enumerations.statuses;

        let isPermanent = !this.get('validPeriod');

        if (isPermanent && status == statuses.success) {
            status = statuses.success;
        }

        if (!isPermanent && status == statuses.success) {
            let expiringDuration = moment.duration(this.get('expiringPeriod'));
            let validDuration = moment.duration(this.get('validPeriod'));

            let expiredMoment = moment(this.get('statusModifiedDate'))
                .add(validDuration);
            let expiringMoment = moment(this.get('statusModifiedDate'))
                .add(validDuration)
                .subtract(expiringDuration);

            let today = moment();
            if (today.isAfter(expiredMoment)) {
                status = statuses.expired;
            } else if (today.isAfter(expiringMoment)) {
                status = statuses.expiring;
            } else {
                status = statuses.success;
            }
        }

        return status;
    }
}).extend(LearningPathService);

export default LearningPath
