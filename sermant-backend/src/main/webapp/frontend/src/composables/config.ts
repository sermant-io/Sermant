import i18n from "~/composables/translations";
import {ref, watch} from "vue";

const resultCodes = [
    {code: '00', key: 'common.success'},
    {code: '01', key: 'common.unableToConnectToConfigurationCenter'},
    {code: '02', key: 'common.failedToObtainConfiguration'},
    {code: '03', key: 'common.configurationAlreadyExists'},
    {code: '04', key: 'common.failedToCreateConfiguration'},
    {code: '05', key: 'common.failedToPublishConfiguration'},
    {code: '06', key: 'common.failedToDeleteConfiguration'},
    {code: '07', key: 'common.configurationDoesNotExist'},
    {code: '08', key: 'common.missingRequestParameters'},
    {code: '09', key: 'common.failedToRequest'},
];

export const resultCodeMap = new Map(resultCodes.map(item => [item.code, i18n.global.t(item.key)]));

watch(() => i18n.global.locale, () => {
    resultCodes.forEach(item => {
        resultCodeMap.set(item.code, i18n.global.t(item.key));
    });
    updateOptions();
});

const options = ref([
    { label: '', value: '' }
]);

function updateOptions() {
    options.value = [
        {label: i18n.global.t('common.router'), value: 'router'},
        {label: i18n.global.t('common.springbootRegistry'), value: 'springboot-registry'},
        {label: i18n.global.t('common.serviceRegistry'), value: 'service-registry'},
        {label: i18n.global.t('common.flowcontrol'), value: 'flowcontrol'},
        {label: i18n.global.t('common.removal'), value: 'removal'},
        {label: i18n.global.t('common.loadbalancer'), value: 'loadbalancer'},
        {label: i18n.global.t('common.tagTransmission'), value: 'tag-transmission'},
        {label: i18n.global.t('common.mqConsumeProhibition'), value: 'mq-consume-prohibition'},
        {label: i18n.global.t('common.databaseWriteProhibition'), value: 'database-write-prohibition'},
        {label: i18n.global.t('common.other'), value: 'other'},
    ];
}

// Initial call to populate options on startup
updateOptions();

export {options};
