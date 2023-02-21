<script setup lang="ts">
import { useRequest } from "vue-request";
import restRequest from "../../../request/restful";
import { useI18n } from "vue-i18n";
import { View, Guide } from "@element-plus/icons-vue";
import Password from "./password.vue";

const { t } = useI18n();

const errorInfo = ref<string | Error>();

const props = withDefaults(
    defineProps<{
        instanceId?: string;
    }>(),
    {
        instanceId: "",
    }
);

onMounted(() => {});
const nodeId = ref<string>("");
const data = reactive<{
    dbParamData: Array<Record<string, string>>;
    osParamData: Array<Record<string, string>>;
}>({
    dbParamData: [],
    osParamData: [],
});

// password dialog
const snapshotManageShown = ref(false);
const showSnapshotManage = () => {
    snapshotManageShown.value = true;
};
const changeModalSnapshotManage = (val: boolean) => {
    snapshotManageShown.value = val;
};

// cluster component
const handleClusterValue = (val: any) => {
    nodeId.value = val.length > 1 ? val[1] : "";
};

const handleQuery = () => {
    showSnapshotManage();
};
const refreshData = (password: string) => {
    requestDBData(password);
    requestOSData(password);
};
const {
    data: res,
    run: requestDBData,
    loading: loadingDBData,
} = useRequest(
    (password) => {
        return restRequest
            .get("/observability/v1/param/databaseParamInfo", {
                nodeId: nodeId.value,
            })
            .then(function (res) {
                return res;
            })
            .catch(function (res) {
                data.dbParamData = [];
            });
    },
    { manual: true }
);
watch(res, (res) => {
    data.dbParamData = res;
});

const {
    data: resOS,
    run: requestOSData,
    loading: loadingOSData,
} = useRequest(
    (password) => {
        return restRequest
            .get("/observability/v1/param/osParamInfo", {
                paramName: "",
                nodeId: nodeId.value,
                dbName: null,
                password,
                isRefresh: null,
                paramType: "",
            })
            .then(function (res) {
                return res;
            })
            .catch(function (res) {
                data.osParamData = [];
            });
    },
    { manual: true }
);
watch(resOS, (resOS) => {
    data.osParamData = resOS;
});
const color = computed(() => {
    console.log('localStorage.getItem("theme")',localStorage.getItem("theme"))
    if (localStorage.getItem("theme") === "dark") return "#fcef92";
    else return "#E41D1D";
});
</script>

<template>
    <div class="" style="padding: 0px 15px">
        <div class="search-form head">
            <div class="filter title" style="margin-right: auto">{{ $t("configParam.tabTitle") }}</div>

            <div class="filter">
                <ClusterCascader notClearable autoSelectFirst :title="$t('datasource.cluterTitle')" @getCluster="handleClusterValue" />
            </div>
            <div class="query filter">
                <el-button type="primary" @click="handleQuery">{{ $t("app.query") }}</el-button>
            </div>
        </div>
        <div class="list">
            <div class="list-item">
                <div class="item-title">{{ $t("configParam.systemConfig") }}</div>
                <div v-loading="loadingOSData">
                    <div class="item-list" v-for="item in data.osParamData" :key="item.seqNo">
                        <div class="item-list-left">
                            <div class="item-name">{{ item.paramName }}</div>
                            <el-popover placement="top-start" :title="$t('configParam.paramDesc')" :width="200" trigger="hover" :content="item.paramDetail">
                                <template #reference>
                                    <el-icon class="detail-btn" :color="'#7d7d7d'" size="18px">
                                        <View />
                                    </el-icon>
                                </template>
                            </el-popover>
                        </div>
                        <div class="item-list-right">
                            <div class="item-value">{{ item.actualValue === undefined || item.actualValue === null ? "--" : item.actualValue }}</div>
                            <div class="suggest-btn-container">
                                <el-popover placement="top-start" :title="$t('configParam.paramTuning')" :width="300" trigger="hover" v-if="item.suggestValue != undefined && item.suggestValue != null && item.suggestValue != '' && item.suggestValue != item.actualValue">
                                    <template #reference>
                                        <el-icon class="suggest-btn" :color="color" size="18px" v-if="item.actualValue != item.suggestValue">
                                            <Guide />
                                        </el-icon>
                                    </template>
                                    <template #default>
                                        <div class="demo-rich-conent" style="display: flex; gap: 16px; flex-direction: column">
                                            <div>
                                                <p class="demo-rich-content__name" style="margin: 0; font-weight: 500">{{ $t("configParam.suggestValue") }}{{ item.suggestValue }}</p>
                                                <p class="demo-rich-content__mention" style="margin: 0; font-size: 14px" v-if="item.suggestExplain">{{ $t("configParam.suggestReason") }}</p>
                                                <p class="demo-rich-content__mention" style="margin: 0; font-size: 14px" v-if="item.suggestExplain">{{ item.suggestExplain }}</p>
                                            </div>
                                        </div>
                                    </template>
                                </el-popover>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="list-item">
                <div class="item-title">{{ $t("configParam.databaseConfig") }}</div>
                <div v-loading="loadingDBData">
                    <div class="item-list" v-for="item in data.dbParamData" :key="item.seqNo">
                        <div class="item-list-left">
                            <div class="item-name">{{ item.paramName }}</div>
                            <el-popover placement="top-start" :title="$t('configParam.paramDesc')" :width="200" trigger="hover" :content="item.paramDetail">
                                <template #reference>
                                    <el-icon class="detail-btn" :color="'#7d7d7d'" size="18px">
                                        <View />
                                    </el-icon>
                                </template>
                            </el-popover>
                        </div>
                        <div class="item-list-right">
                            <div class="item-value">{{ item.actualValue === undefined || item.actualValue === null ? "--" : item.actualValue }}</div>
                            <div class="suggest-btn-container">
                                <el-popover placement="top-start" :title="$t('configParam.paramTuning')" :width="300" trigger="hover" v-if="item.suggestValue != undefined && item.suggestValue != null && item.suggestValue != '' && item.suggestValue != item.actualValue">
                                    <template #reference>
                                        <el-icon class="suggest-btn" :color="color" size="18px" v-if="item.actualValue != item.suggestValue">
                                            <Guide />
                                        </el-icon>
                                    </template>
                                    <template #default>
                                        <div class="demo-rich-conent" style="display: flex; gap: 16px; flex-direction: column">
                                            <div>
                                                <p class="demo-rich-content__name" style="margin: 0; font-weight: 500">{{ $t("configParam.suggestValue") }}{{ item.suggestValue }}</p>
                                                <p class="demo-rich-content__mention" style="margin: 0; font-size: 14px" v-if="item.suggestExplain">{{ $t("configParam.suggestReason") }}</p>
                                                <p class="demo-rich-content__mention" style="margin: 0; font-size: 14px" v-if="item.suggestExplain">{{ item.suggestExplain }}</p>
                                            </div>
                                        </div>
                                    </template>
                                </el-popover>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <my-message v-if="errorInfo" type="error" :tip="errorInfo" defaultTip="" />
        <Password :show="snapshotManageShown" @changeModal="changeModalSnapshotManage" @confirm="refreshData" />
    </div>
</template>

<style scoped lang="scss">
@import "../../../assets/style/style1.scss";
.head {
    display: flex;
    align-items: center;
    margin-bottom: 15px;
    .title {
        font-size: 16px;
        font-weight: bold;
    }
}
.list {
    display: flex;
    flex-direction: row;
    .list-item {
        width: 50%;
        .item-title {
            font-size: 14px;
            font-weight: bold;
            margin-bottom: 5px;
        }
        .item-list {
            display: flex;
            margin: 5px 0px;
            .item-list-left {
                width: 55%;
                display: flex;
                align-items: center;
                flex-shrink: 0;
                .detail-btn {
                    margin-left: 10px;
                }
            }
            .item-list-right {
                width: 40%;
                display: flex;
                align-items: center;
                overflow: hidden;
                padding-right: 20px;
                padding-left: 20px;
                vertical-align: middle;
                .item-value {
                    display: inline-block;
                    white-space: nowrap;
                    overflow: hidden;
                    text-overflow: ellipsis;
                    text-align: left;
                }
                .suggest-btn-container {
                    width: 20px;
                    display: flex;
                }
                .suggest-btn {
                    margin-left: 5px;
                }
            }
        }
    }
}
</style>