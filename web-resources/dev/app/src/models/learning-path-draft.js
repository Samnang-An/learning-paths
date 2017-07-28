import ValamisModel from 'models/valamis';
import appConfig from "config";
import App from 'application';

const LearningPathDraftService = new Backbone.Service({
    url: function (model) {
        return `${appConfig().endpoint.learningPaths}`
    },
    sync: {
        read: {
            path: 'draft',
            method: 'get'
        },
        update: {
            path: 'draft',
            method: 'put'
        }
    },
    targets: {
        deleteLogo: {
            path: 'draft/logo',
            method: 'delete'
        },
        addGoals: {
            path: 'draft/goals',
            data: (model, options) => {
                return options.data
            },
            method: 'post'
        },
        addGroup: {
            path: 'draft/groups',
            data: (model, options) => {
                return {title: options.title}
            },
            method: 'post'
        },
        publish: {
            path: 'draft/publish',
            method: 'post'
        },
        addRecommendation: {
            path: 'draft/recommended-competences',
            data: (model, options) => {
                return {
                    skillId: options.skillId,
                    skillName: options.skillName,
                    levelId: options.levelId,
                    levelName: options.levelName
                }
            },
            method: 'post'
        }
    }
});

const LearningPathDraft = ValamisModel.extend({
    defaults: {
        title: '',
        logo: '',
        openBadgesEnabled: false,
        hasDraft: true
    },
    getFullLogoUrl() {
        let logoUrl = this.get('logoUrl');
        return (logoUrl) ? `${appConfig().apiPath}${logoUrl}` : ''
    },
    save(key, val, options) {
        if (this.get('title') === '') {
            this.set('title', App.language['defaultNameLabel']);
        }

        return ValamisModel.prototype.save.apply(this, [key, val, options]);
    }
}).extend(LearningPathDraftService);

export default LearningPathDraft
