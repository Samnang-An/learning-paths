import template from './content-in-iframe.html';

import App from 'application';
import LiferayUtil from 'utils/liferay-utils';

import LessonModel from 'models/lesson';

const Lesson = Marionette.View.extend({
    template: template,
    className: 'lesson js-lesson',
    ui: {
        iframe: 'iframe',
        loading: '.js-loading',
        closeLesson: '.js-player-navigation-exit',
        lessonTimeout: '.js-lesson-timeout',
        hintSCORMlesson: '.js-scorm-lesson-hint',
        failedMessage: '.js-failed-message'
    },
    events: {
        'click @ui.closeLesson': 'closeLesson'
    },
    templateContext() {
        return {
            isLesson: true,
            lessonUrl: this.lesson.getLessonUrl()
        }
    },
    initialize() {
        this.lesson = new LessonModel({id: this.options.lessonId});
    },
    onRender() {
        this.ui.iframe.on('load', (e) => {
            if (e.target.src != '') {
                this.ui.loading.addClass('hidden');
            }
        });

        this.lesson.getLessonInfo().then(
            (response) => { this.checkLessonType(response.lessonType); },
            () => { this.showFailedMessage('failedLessonLoadingLabel'); }
        );
    },
    checkLessonType(lessonType) {
        // now only support of tincan lessons was implemented
        if (lessonType === 'tincan') {
            this.loadTincanPackage();
        }
        else {
            this.ui.hintSCORMlesson.removeClass('hidden');
            this.ui.iframe.remove();
            this.ui.loading.addClass('hidden');
        }
    },
    closeLesson() {
        this.ui.iframe.remove();
        this.ui.lessonTimeout.removeClass('hidden');
    },
    loadTincanPackage() {
        let defInfo = $.Deferred();
        let defSettings = $.Deferred();

        this.lesson.getTincanLessonInfo().then(
            (data) => { defInfo.resolve(data); },
            (status, jqXHR) => { defInfo.reject(status, jqXHR); }
        );

        this.lesson.getLrsSettings().then(
            (data) => { defSettings.resolve(data); },
            (status, jqXHR) => { defSettings.reject(status, jqXHR); }
        );

        $.when( defInfo, defSettings ).then(
            (lessonData, lrsData) => { this.openTincanPackage(lessonData, lrsData) },
            (status, jqXHR) => {
                let failedMessageLabel = jqXHR.responseText.includes('unavailablePackageException')
                    ? 'unavailableLessonMessageLabel'
                    : 'failedLessonLoadingLabel';
                this.showFailedMessage(failedMessageLabel);
            }
        );
    },
    showFailedMessage(messageLabel) {
        this.ui.loading.addClass('hidden');
        this.ui.iframe.remove();
        this.ui.failedMessage.removeClass('hidden');
        this.ui.failedMessage.text(App.language[messageLabel]);
    },
    openTincanPackage(lessonData, lrsData) {
        let lrsParams = 'endpoint={0}&auth={1}&actor={2}&activity_id={3}&v={4}'
            .replace('{0}', encodeURIComponent(lrsData.endpoint))
            .replace('{1}', encodeURIComponent(lrsData.auth))
            .replace('{2}', encodeURIComponent(JSON.stringify(lrsData.agent)))
            .replace('{3}', lessonData.activityId)
            .replace('{4}', lessonData.versionNumber);

        let src = '{0}SCORMData/{1}?locale={2}&{3}'
            .replace('{0}', lrsData.valamisContextPath)
            .replace('{1}', lessonData.launchURL)
            .replace('{2}', LiferayUtil.getUserLocale())
            .replace('{3}', lrsParams);

        this.ui.iframe.attr('src', src);
    }
});

export default Lesson;