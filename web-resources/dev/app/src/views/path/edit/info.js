import template from './info.html';

import App from 'application';
import appConfig from 'config';
import Radio from 'backbone.radio';
import LiferayUtil from 'utils/liferay-utils';
import FileUploaderService from 'services/file-uploader-service';

import * as enumerations from 'models/enumerations';
import Course from 'models/course';

import PathExtEdit from './ext-info';
import PathGoalsEdit from '../goals-list/goals-edit';
import ModalView from 'modals/modal-view';
import SelectScopeLayout from './select-scope/layout';

import SkillSelect from './skill-select';
import LevelSelect from './level-select';
import Skills from 'collections/skills';
import Levels from 'collections/levels';

import Competencies from 'collections/competencies';
import CompetenciesView from './competencies'

const fileUploader = new FileUploaderService();

const PathInfo = Marionette.View.extend({
    className: 'preview-wrapper',
    template: template,
    templateContext() {
        let goalsToOmit = [enumerations.goalTypes.group];
        if (!appConfig().isAssignmentDeployed) {
            goalsToOmit.push(enumerations.goalTypes.assignment);
        }
        if (!appConfig().isTrainingEventDeployed) {
            goalsToOmit.push(enumerations.goalTypes.trainingEvent);
        }
        if (!appConfig().isValamisDeployed) {
            goalsToOmit.push(enumerations.goalTypes.statement);
            goalsToOmit.push(enumerations.goalTypes.lesson);
        }

        let goals = _.omit(enumerations.goalTypes, goalsToOmit);
        let goalTypesList = _.map(goals, (value, key) => {
            return {value: value, label: App.language[key + 'GoalAddLabel']};
        });

        return {
            fullLogoUrl: this.model.getFullLogoUrl(),
            goalTypesList: goalTypesList
        }
    },
    ui: {
        title: '.js-title',
        description: '.js-description',
        changeScope: '.js-change-scope',
        removeScope: '.js-remove-scope',
        scopeTitle: '.js-scope-title',
        uploadLogo: '.js-upload-image',
        designLogo: '.js-design-new-badge',
        deleteLogo: '.js-delete-logo',
        logo: '.js-logo',
        addGoal: '.js-add-goal li:not(.js-add-group)',
        addGroup: '.js-add-goal li.js-add-group',
        addRecSkills: '.js-add-prerequisites',
        addImprovingSkills: '.js-add-improving-skills',
        selectDropdown: '.js-select-dropdown',
        competenceButtons: '.js-show-list',
        commonSelectLabel: '.js-show-select-label',
        selectSkillLabel: '.js-show-select-skill-label',
        selectLevelLabel: '.js-show-select-level-label'
    },
    regions: {
        recommendationList: '.js-recommendation-list',
        improvingList: '.js-improving-list',
        prerequisites: {
            el: '.js-prerequisites',
            replaceElement: true
        },
        improvingSkills: {
            el: '.js-improving-skills',
            replaceElement: true
        },
        goals: '#pathGoals .js-path-goals',
        extInfo: '#pathExtInfo'
    },
    modelEvents: {
        // model can be also changed in child view
        'path:save': 'savePath'
    },
    events: {
        'click': 'closeDropdown',
        'change @ui.title': 'saveTitle',
        'change @ui.description': 'saveDescription',
        'click @ui.changeScope': 'setScope',
        'click @ui.removeScope': 'unsetScope',
        'click @ui.uploadLogo': 'uploadLogo',
        'click @ui.designLogo': 'openBadgeDesigner',
        'click @ui.deleteLogo': 'onLogoDelete',
        'click @ui.addGoal': 'onClickAddGoal',
        'click @ui.addGroup': 'onAddGroup',
        'click @ui.addRecSkills': 'showRecSkillsList',
        'click @ui.addImprovingSkills': 'showImprovingSkills'
    },
    onRender() {
        let extInfo = new PathExtEdit({model: this.model});
        this.showChildView('extInfo', extInfo);

        if (this.model.get('courseId')) {
            let course = new Course({id: this.model.get('courseId')});
            course.fetch().then(
                () => {
                    this.updateScopeLayout(course.get('title'));
                },
                () => {
                    Radio.channel('notify').trigger('notify', 'error', 'failedLabel');
                }
            );
        }
        else {
            this.updateScopeLayout();
        }

        // temporary
        this.$('.js-dropdown').valamisDropDown();

        this.showGoals();

        this.prepareBadgeListener();

        // this.recommendations = new Competencies();
        // this.recommendations.on('remove', (model) => {
        //     this.recommendations.deleteCompetency({}, {
        //         skillId: model.get('skillId')
        //     })
        // });
        //
        // this.showChildView('recommendationList', new CompetenciesView({
        //     collection: this.recommendations
        // }));
        //
        // this.improvements = new Competencies();
        // this.improvements.on('remove', (model) => {
        //     this.improvements.deleteCompetency({}, {
        //         skillId: model.get('skillId'),
        //         improving: true
        //     })
        // });
        //
        // if (!!this.model.get('id')) {
        //     this.recommendations.pathId = this.model.get('id') + '/draft';
        //     this.recommendations.fetch().then(() => {
        //         this.recommendations.on('update', () => {
        //             this.availableRecSkills = this.updateAvailableSkills(this.recommendations)
        //         }, this);
        //     });
        //
        //     this.improvements.pathId = this.model.get('id') + '/draft';
        //     this.improvements.fetch({
        //         improving: true
        //     }).then(() => {
        //         this.improvements.on('update', () => {
        //             this.availableImprovingSkills = this.updateAvailableSkills(this.improvements)
        //         }, this);
        //     });
        // }
        //
        // this.showChildView('improvingList', new CompetenciesView({
        //     collection: this.improvements
        // }));
        //
        // this.levels = new Levels();
        // this.levels.fetch();
        // this.levels.on('levelSelected', this.addCompetency, this);
        //
        // this.skills = new Skills();
        // this.skills.fetch().then(() => {
        //     this.availableRecSkills = this.updateAvailableSkills(this.recommendations);
        //     this.availableImprovingSkills = this.updateAvailableSkills(this.improvements);
        // });
        // this.skills.on('skillSelected', this.showLevelsList, this);
    },
    updateAvailableSkills(recommendations) {
        let availableSkills;
        if (recommendations && recommendations.length > 0) {
            let availableSkillsArray = this.skills.filter((model) => {
                return recommendations.where({skillId: model.get('id')}).length == 0;
            });
            availableSkills = new Skills(availableSkillsArray);
        }
        else {
            availableSkills = this.skills;
        }
        return availableSkills;
    },
    showGoals() {
        let pathId = (!!this.model.get('id')) ? this.model.get('id') + '/draft' : '';
        let goalsView = new PathGoalsEdit({
            pathId: pathId,
            editView: true
        });
        this.showChildView('goals', goalsView);
    },
    saveTitle() {
        this.model.set('title', this.ui.title.val());
        this.model.trigger('path:save');
    },
    saveDescription() {
        this.model.set('description', this.ui.description.val());
        this.model.trigger('path:save');
    },
    setScope() {
        let modalChannel = Radio.channel('modal');

        let scopesView = new SelectScopeLayout();
        let scopesModalView = new ModalView({
            contentView: scopesView,
            header: App.language['selectScopeLabel']
        });

        scopesView.on('scope:selected', (scopeInfo) => {
            modalChannel.trigger('close', scopesModalView);

            this.updateScopeLayout(scopeInfo.title);

            this.model.set({'courseId': scopeInfo.id});
            this.model.trigger('path:save');
        });
        scopesView.on('modal:close', () => {
            modalChannel.trigger('close', scopesModalView);
        });

        modalChannel.trigger('open', scopesModalView);
    },
    updateScopeLayout(title) {
        let label = (!!title) ? title : App.language['instanceScopeLabel'];
        this.ui.scopeTitle.text(label);
        this.ui.removeScope.toggleClass('hidden', !title);
    },
    unsetScope() {
        this.model.unset('courseId');
        this.updateScopeLayout('');
        this.model.trigger('path:save');
    },
    savePath(callback_func) {
        let oldTitle = this.model.get('title');
        this.model.save().then(
            () => {
                if (oldTitle !== this.model.get('title')) {
                    this.ui.title.val(this.model.get('title'));
                }
                if (_.isFunction(callback_func)) {
                    callback_func();
                }
            },
            () => {
                Radio.channel('notify').trigger('notify', 'error', 'failedLabel');
            }
        );
    },
    uploadLogo() {
        let options = {
            'autoUpload': true,
            'fileAttribute': 'image',
            'message': App.language['uploadLogoMessage'],
            'header': App.language['fileUploadMessage']
        };
        if (!this.model.get('id')) {
            this.savePath(() => {
                fileUploader.upload(options, this);
            });
        }
        else {
            fileUploader.upload(options, this);
        }
    },
    uploadBadgesHandler(e){
        const that = this;
        const url = that.getImageUploadUrl(that.model.get('id'));

        if (e.origin != 'https://www.openbadges.me' || e.data == 'cancelled') return;

        fileUploader.uploadBadges(e, url).done(function (data) {
            that.triggerMethod('fileUploaded', data.logoUrl);
        });
    },
    // once we can't catch events from the pop-up's document from a different domain
    // attach listener when view is rendered and detach it on destroy
    prepareBadgeListener() {
        this.bindedHandler = this.uploadBadgesHandler.bind(this);
        window.addEventListener('message', this.bindedHandler);
    },
    removeBadgeListener() {
        window.removeEventListener('message', this.bindedHandler);
    },
    openBadgeDesigner() {
        let URL = 'https://www.openbadges.me/designer.html?origin=http://' + appConfig().root +
            '&email=developer@example.com&close=true';
        let options = 'width=1015,height=680,location=0,menubar=0,status=0,toolbar=0';

        if (!this.model.get('id')) {
            this.savePath(() => {
                window.open(URL, '', options);
            });
        }
        else {
            window.open(URL, '', options);
        }
    },
    onFileUploaded(url) {
        this.ui.deleteLogo.removeClass('hidden');
        this.model.set('logoUrl', url);
        this.ui.logo.attr('src', this.model.getFullLogoUrl());
    },
    getImageUploadUrl(id) {
        return `${appConfig().apiPath}${appConfig().endpoint.learningPaths}` + id
            + '/draft/logo?layoutId=' + LiferayUtil.plId();
    },
    onLogoDelete() {
        this.ui.logo.attr('src', '');
        this.ui.deleteLogo.addClass('hidden');

        this.model.deleteLogo().then(
            () => {
                this.model.unset('logoUrl');
            }
        );
    },
    onClickAddGoal(e) {
        let goalType = $(e.target).data('value');
        this.savePath(() => {
            this.triggerMethod('select:goals:step1', goalType);
        });
    },
    onAddGroup() {
        this.savePath(() => {
            this.addGroup()
        });
    },
    addGroup() {
        this.model.addGroup({}, {
            title: App.language['newGroupLabel']
        }).then(
            () => {
                this.showGoals();
            } // todo find better solution!!
        );
    },
    onDestroy() {
        this.removeBadgeListener();
    },
    showRecSkillsList () {
        const button = this.ui.addRecSkills;
        this.toggleSelectTitle(button.find(this.ui.selectSkillLabel), button);
        const skills = new SkillSelect({
            collection: this.availableRecSkills
        });
        this.showChildView('prerequisites', skills);
        this.showDropdown(button);
    },
    showImprovingSkills () {
        const button = this.ui.addImprovingSkills;
        this.toggleSelectTitle(button.find(this.ui.selectSkillLabel), button);
        const skills = new SkillSelect({
            collection: this.availableImprovingSkills,
            improving: true
        });
        this.showChildView('improvingSkills', skills);
        this.showDropdown(button);
    },
    showLevelsList(options) {
        if (options.improving) {
            this.showImprovingLevelList(options.model);
        }
        else {
            this.showRecLevelList(options.model);
        }

    },
    showRecLevelList(model) {
        const button = this.ui.addRecSkills;
        this.toggleSelectTitle(button.find(this.ui.selectLevelLabel), button);
        const levels = new LevelSelect({
            collection: this.levels,
            skillName: model.title,
            skillId: model.id
        });
        this.showChildView('prerequisites', levels);
        this.showDropdown(button);
    },
    showImprovingLevelList(model) {
        const button = this.ui.addImprovingSkills;
        this.toggleSelectTitle(button.find(this.ui.selectLevelLabel), button);
        const levels = new LevelSelect({
            collection: this.levels,
            skillName: model.title,
            skillId: model.id,
            improving: true
        });
        this.showChildView('improvingSkills', levels);
        this.showDropdown(button);
    },
    toggleSelectTitle(el, skills) {
        skills.find('span').addClass('hidden');
        el.removeClass('hidden');
    },
    closeDropdown(e) {
        if (!!e) {
            const el = $(e.target);
            if (!(
                (el.closest(this.ui.addRecSkills).length > 0) ||
                (el.closest(this.ui.addImprovingSkills).length > 0) ||
                (el.closest('.js-search input').length > 0)
                ) &&
                !(el.closest('.js-select-item').length > 0)) {
                const dropdownMenu = this.$(this._uiBindings.selectDropdown);
                dropdownMenu.removeClass('dropdown-visible');
                this.toggleSelectTitle(
                    this.ui.commonSelectLabel,
                    this.ui.competenceButtons
                );
            }
        }
        else {
            const dropdownMenu = this.$(this._uiBindings.selectDropdown);
            dropdownMenu.removeClass('dropdown-visible');
            this.toggleSelectTitle(
                this.ui.commonSelectLabel,
                this.ui.competenceButtons
            );
        }
    },
    showDropdown(button) {
        const dropdownMenu = button.parent().find(this._uiBindings.selectDropdown);
        dropdownMenu.addClass('dropdown-visible');
    },
    addCompetency(model, options) {
        this.savePath(() => {
            if (!this.recommendations.pathId) {
                this.recommendations.pathId = this.model.get('id') + '/draft';
            }
            if (!this.improvements.pathId) {
                this.improvements.pathId = this.model.get('id') + '/draft';
            }
            const data = {
                skillId: options.skillId,
                skillName: options.skillName,
                levelId: options.model.attributes.id,
                levelName: options.model.attributes.title,
                improving: options.improving
            };
            if (data.improving) {
                this.addImprovement(data);
            }
            else {
                this.addRecommendation(data);
            }
        });
    },
    addRecommendation(data) {
        this.recommendations.addCompetency({}, {
            data: data
        }).then(() => {
            this.recommendations.add(data);
        });
    },
    addImprovement(data) {
        this.improvements.addCompetency({}, {
            data: data
        }).then(() => {
            this.improvements.add(data);
        });
    }
});

export default PathInfo;