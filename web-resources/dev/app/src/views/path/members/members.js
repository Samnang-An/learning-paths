import template from './member-item.html';
import mainTemplate from './member-layout.html';
import toolbarTemplate from './members-toolbar.html';

import App from 'application';
import RootModel from 'models/root';
import Radio from 'backbone.radio';
import * as Paging from 'paging/paging';
import * as enumerations from 'models/enumerations';
import ValamisUIControls from 'behaviors/valamis-ui-controls';

import EmptyMembersView from './empty-members';

const MemberItemView = Marionette.View.extend({
    tagName: 'tr',
    template: template,
    initialize() {
        this.isMemberUser = this.model.collection.memberType == enumerations.memberType.user;
    },
    templateContext() {
        // todo show tooltip !!
        let tooltipTemplate = function (text) {
            return '<span class="valamis-tooltip" data-placement="bottom" title="'
                + App.language['memberAsPartOfGroupHintLabel'] + '">'
                + text + '</span>';
        };

        let info = '';
        let model = this.model;

        if (this.isMemberUser) {
            let tpe = enumerations.memberType;

            let matchTpe = {};
            matchTpe[tpe.organization] = 'organization';
            matchTpe[tpe.userGroup] = 'group';
            matchTpe[tpe.role] = 'role';

            let membershipInfo = model.get('membershipInfo');

            info = _.map([tpe.organization, tpe.userGroup, tpe.role], (group) => {
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
        }
        else {
            info = this.model.get('userCount') + ' ' + App.language['membersLabel'];
        }

        let memberStatus = this.model.get('status');
        let progressPercent = Math.round((this.model.get('progress') || 0) * 100);
        let statuses = enumerations.statuses;

        return {
            available: this.options.available,
            cid: this.cid,
            info: info,
            isGroup: !this.isMemberUser,
            progressPercent: progressPercent,
            isInProgress: memberStatus === statuses.inprogress,
            isSucceed: memberStatus === statuses.success,
            isFailed: memberStatus === statuses.failed,
            isExpired: memberStatus === statuses.expired,
            isExpiring: memberStatus === statuses.expiring
        }
    },
    ui: {
        'delete': '.js-member-delete',
        'checkbox': '.val-checkbox',
        'tooltip': '.valamis-tooltip'
    },
    events: {
        'click': 'openProgress',
        'click @ui.delete': 'memberDelete',
        'change @ui.checkbox': 'toggleModelSelected'
    },
    modelEvents: {
        'change:selected': 'toggleSelected'
    },
    onRender() {
        if (this.isMemberUser) {
            this.$el.addClass('cursor-pointer');
        }
    },
    toggleModelSelected: function (e) {
        // change event fires only in case of user activity
        // so it will not be triggered when select all members happens
        let isSelected = $(e.target).prop('checked');
        this.model.set('selected', isSelected);

        let userCount = this.model.get('userCount');
        // if userCount is not set, then single user was toggled
        if (userCount == undefined) {
            userCount = 1;
        }
        this.model.collection.trigger('members:selected:changed',
            (isSelected) ? userCount : -1 * userCount);
    },
    toggleSelected: function () {
        this.ui.checkbox.prop('checked', this.model.get('selected'));
    },
    memberDelete: function (event) {
        event.stopPropagation();

        let collection = this.model.collection;
        this.model.destroy({
            data: {
                memberIds: this.model.get('id'),
                memberType: collection.memberType
            }
        }).then(
            () => {
                collection.trigger('member:removed');
            },
            () => {
                Radio.channel('notify').trigger('notify', 'error', 'failedLabel');
            }
        );
    },
    openProgress() {
        if (this.isMemberUser) {
            this.model.trigger('show:user:progress', this.model.collection.indexOf(this.model));
        }
    }
});

const MembersListView = Marionette.CollectionView.extend({
    tagName: 'table',
    className: 'val-table',
    childView: MemberItemView,
    childViewOptions: function () {
        return {
            available: this.options.available
        }
    }
});

const Toolbar = Marionette.View.extend({
    template: toolbarTemplate,
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
            membersType: membersType,
            addMembers: addMembers,
            available: this.options.available,
            cid: this.cid
        }
    },
    behaviors: [ValamisUIControls],
    ui: {
        'search': '.js-search input',
        'sort': '.js-sort-filter',
        'memberType': '.js-member-type',
        'addMembers': '.js-add-members',
        'selectAll': '.js-select-all',
        'deselectAll': '.js-deselect-all',
        'selectedInfo': '.js-selected-info',
        'selectedAmount': '.js-selected-amount'
    },
    events: {
        'keyup @ui.search': 'changeSearchText',
        'click @ui.sort li': 'changeSort',
        'click @ui.memberType li': 'changeMemberType',
        'click @ui.addMembers li': 'addMembers',
        'change @ui.selectAll': 'toggleSelectAll',
        'click @ui.deselectAll': 'deselectAll'
    },
    onRender: function () {
        this.isSelectedAll = false;
        this.selectedMembers = 0;

        this.on('valamis:controls:init', function () {
            let key = _.findKey(enumerations.memberType,
                (i) => {
                    return i == this.model.get('memberType')
                });
            this.ui.memberType.valamisDropDown('select', key);
        }, this);

    },
    changeSearchText: function (e) {
        clearTimeout(this.inputTimeout);
        this.inputTimeout = setTimeout(() => {
            this.model.set('searchtext', $(e.target).val());
        }, 800);
    },
    changeSort: function (e) {
        this.model.set('sort', $(e.target).attr('data-value'));
    },
    changeMemberType: function (e) {
        let memberType = enumerations.memberType[$(e.target).attr('data-value')];
        this.model.set('memberType', memberType);
        this.deselectAll();
    },
    addMembers: function (e) {
        let memberType = enumerations.memberType[$(e.target).attr('data-value')];
        this.triggerMethod('add:member', memberType);
    },
    toggleSelectAll: function () {
        this.isSelectedAll = this.ui.selectAll.prop('checked');
        this.selectedMembers = (this.isSelectedAll)
            ? this.options.paginatorModel.get('membersSum') : 0;

        this.updateSelectedText(this.isSelectedAll);
        this.triggerMethod('toggle:select:all', this.isSelectedAll);
    },
    deselectAll: function () {
        this.ui.selectAll.prop('checked', false);
        this.toggleSelectAll();
    },
    updateSelectedText: function (isSmthSelected) {
        this.ui.selectedInfo.toggleClass('hidden', !isSmthSelected);
        this.ui.selectedAmount.text(this.selectedMembers);
    },
    checkSelectedAmount: function (difference, isSmthSelected) {
        this.selectedMembers += difference;
        this.ui.selectAll.prop('checked',
            this.selectedMembers == this.options.paginatorModel.get('membersSum'));
        this.updateSelectedText(isSmthSelected);
    }
});

const MembersView = Marionette.View.extend({
    template: mainTemplate,
    templateContext: function () {
        return {available: this.options.available}
    },
    regions: {
        toolbar: '#viewToolbar',
        list: '#viewList',
        more: '#viewPaginatorMore',
        showing: '#viewPaginatorShowing',
        noMembers: '#viewNoMembers'
    },
    ui: {
        back: '.js-back',
        add: '.js-add-selected',
        hintLayout: '.js-hint-layout',
        contentLayout: '.js-content-layout',
        selectMembers: '.js-select-members'
    },
    events: {
        'click @ui.back': 'goBack',
        'click @ui.add': 'addSelectedMembers',
        'click @ui.selectMembers': 'selectMembers'
    },
    childViewEvents: {
        'add:member': 'selectMembers',
        'toggle:select:all': 'toggleSelectAll'
    },
    initialize: function () {
        this.paginatorModel = new Paging.PageModel({itemsOnPage: 10, isLazy: true});
        this.members = this.options.members;
        this.members.memberType = this.options.memberType || enumerations.memberType.user;

        this.filter = new RootModel({
            sort: 'name',
            memberType: this.members.memberType
        });

        this.available = !!this.options.available;
        this.members.on('collection:update', this.fetch, this);
    },
    selectMembers: function (memberType) {
        this.triggerMethod('select:members', memberType);
    },
    toggleSelectAll: function (selected) {
        this.members.each((i) => {
            i.set('selected', selected)
        });
    },
    onRender: function () {
        if (!this.available) {
            this.showChildView('noMembers', new EmptyMembersView());
        }

        let toolbarView = new Toolbar({
            model: this.filter,
            paginatorModel: this.paginatorModel,
            available: this.available
        });
        this.showChildView('toolbar', toolbarView);

        this.filter.on('change', (model) => {
            toolbarView.deselectAll();
            this.fetch();
        });

        this.members.on('member:removed', () => {
            this.fetch();
            this.triggerMethod('preview:path');
        });
        this.members.on('member:update', () => {
            this.fetch();
        });
        this.members.on('sync', (collection) => {
            this.toggleLoading(false, !collection.hasItems());

            let newFields = {totalElements: collection.options.total};
            if (!!collection.available) {
                _.extend(newFields, {membersSum: collection.getMembersSum()});
            }
            this.paginatorModel.set(newFields);

            this.toggleHint();
        });
        this.paginatorModel.on('pageChanged', () => {
            this.fetchMore()
        });
        this.members.on('members:selected:changed', (difference) => {
            let isSmthSelected = this.members.filter({'selected': true}).length > 0;
            toolbarView.checkSelectedAmount(difference, isSmthSelected);
        }, this);

        let listView = new MembersListView({
            collection: this.members,
            available: this.available,
            memberType: this.members.memberType
        });
        this.showChildView('list', listView);

        let paginatorShowMoreView = new Paging.ValamisPaginatorShowMore({
            model: this.paginatorModel
        });
        this.showChildView('more', paginatorShowMoreView);

        let paginatorShowingView = new Paging.ValamisPaginatorShowing({
            model: this.paginatorModel
        });
        this.showChildView('showing', paginatorShowingView);

        this.fetch();
    },
    onDestroy() {
        // destroy all events for case when view is destroyed before request complete
        this.members.off();
    },
    toggleLoading: function (loading, emptyList) {
        this.$('.js-loading-container').toggleClass('hidden', !loading);
        this.$('.js-info-label').toggleClass('hidden', loading || !emptyList)
    },
    fetch: function () {
        this.paginatorModel.set({'totalElements': 0});
        this.members.memberType = this.filter.get('memberType');
        this.members.reset();

        let count = this.paginatorModel.get('currentPage') * this.paginatorModel.get('itemsOnPage');
        this.fetchMore(1, count);
    },
    fetchMore: function (page, count) {
        this.toggleLoading(true);

        let take = count || this.paginatorModel.get('itemsOnPage');
        let skip = ((page || this.paginatorModel.get('currentPage')) - 1) * take;

        this.members.fetch({
            data: {
                skip: skip,
                take: take,
                name: this.filter.get('searchtext'),
                sort: this.filter.get('sort')
            },
            add: true,
            remove: false,
            merge: false
        }).then(
            () => {},
            () => {
                Radio.channel('notify').trigger('notify', 'error', 'failedLabel');
                this.toggleLoading(false);
            }
        );
    },
    toggleHint() {
        let showHint = !this.available && this.members.length == 0
            && this.filter.get('memberType') == enumerations.memberType.user
            && !this.filter.get('searchtext');
        this.ui.hintLayout.toggleClass('hidden', !showHint);
        this.ui.contentLayout.toggleClass('hidden', showHint);
    },
    addSelectedMembers: function () {

        let selected = this.members.where({selected: true});
        if (selected.length == 0) {
            Radio.channel('notify').trigger('notify', 'warning', 'noUsersSelectedLabel');
            return false;
        }

        let selectedIds =  _.map(selected, function (i) {
            return i.get('id')
        });

        this.options.path.set('userMembersCount', this.members.getMembersSum());
        this.options.path.addMembers({}, {
            memberType: this.members.memberType,
            selectedIds: selectedIds
        }).then(
            () => { this.goBack() },
            () => { Radio.channel('notify').trigger('notify', 'error', 'failedLabel'); }
        );
    },
    goBack() {
        this.triggerMethod('select:members:back');
    }
});

export default MembersView