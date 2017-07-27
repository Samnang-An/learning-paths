import template from './user-progress.html';
import userInfoTemplate from 'views/path/members/member-item.html';

import App from 'application';
import * as enumerations from 'models/enumerations';

import PathGoalsPreview from '../goals-list/goals-preview';

const UserInfoPreview = Marionette.View.extend({
    template: userInfoTemplate,
    tagName: 'tr',
    templateContext: function () {
        // todo show tooltip !!
        let tooltipTemplate = function (text) {
            return '<span class="valamis-tooltip" data-placement="bottom" title="'
                + App.language['memberAsPartOfGroupHintLabel'] + '">'
                + text + '</span>';
        };


        let model = this.model;
        let tpe = enumerations.memberType;

        let matchTpe = {};
        matchTpe[tpe.organization] = 'organization';
        matchTpe[tpe.userGroup] = 'group';
        matchTpe[tpe.role] = 'role';

        let membershipInfo = model.get('membershipInfo');

        let info = _.map([tpe.organization, tpe.userGroup, tpe.role], (group) => {
            return model.get(group).map(function (i) {
                let match = _.filter(membershipInfo, function (mi) {
                    return mi.tpe === matchTpe[group] && mi.id === i.id
                });
                if (match.length > 0) {
                    return tooltipTemplate(i.name);
                }
                else {
                    return i.name
                }
            }).join(', ');
        });
        info = _.filter(info, function (i) {
            return i != ''
        }).join(' • ');


        let memberStatus = this.model.get('status');
        let progressPercent = Math.round((this.model.get('progress') || 0) * 100);
        let statuses = enumerations.statuses;

        return {
            cid: this.cid,
            info: info,
            progressPercent: progressPercent,
            isInProgress: memberStatus === statuses.inprogress,
            isSucceed: memberStatus === statuses.success,
            isFailed: memberStatus === statuses.failed,
            isExpired: memberStatus === statuses.expired,
            isExpiring: memberStatus === statuses.expiring
        }
    }
});

const UserProgressPreview  = Marionette.View.extend({
    className: 'user-progress',
    template: template,
    regions: {
        info: '.js-user-info',
        goals: '.js-user-goals'
    },
    ui: {
        prevUser: '.js-previous-user',
        nextUser: '.js-next-user',
        back: '.js-back'
    },
    events: {
        'click @ui.prevUser': 'showForPrevUser',
        'click @ui.nextUser': 'showForNextUser',
        'click @ui.back': 'goBack'
    },
    onRender() {
        this.userIndex = this.options.userIndex || 0;
        this.showProgress();
    },
    showForPrevUser() {
        if (this.userIndex > 0) {
            this.userIndex--;
            this.showProgress();
        }
    },
    showForNextUser() {
        if (this.userIndex < this.options.members.length) {
            this.userIndex++;
            this.showProgress();
        }
    },
    showProgress() {
        let userModel = this.options.members.at(this.userIndex);
        if (userModel) {
            this.ui.prevUser.prop('disabled', this.userIndex == 0);
            this.ui.nextUser.prop('disabled', this.userIndex == this.options.members.length - 1);

            let userInfo = new UserInfoPreview({
                model: userModel
            });
            this.showChildView('info', userInfo);

            let goalsView = new PathGoalsPreview({
                path: this.options.path,
                doAsAdmin: false,
                versionId: userModel.get('statusVersionId'),
                userId: userModel.get('id')
            });
            this.showChildView('goals', goalsView);
        }
    },
    goBack() {
        this.triggerMethod('user:progress:back');
    }
});

export default UserProgressPreview;