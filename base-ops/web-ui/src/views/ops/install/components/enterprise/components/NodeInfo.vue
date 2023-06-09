<template>
  <div class="node-form-c">
    <a-form :model="form" :rules="formRules" auto-label-width ref="formRef">
      <a-form-item field="hostId" :label="$t('enterprise.NodeConfig.5mpme7w6azo0')">
        <a-select :loading="data.hostListLoading" v-model="form.hostId" @change="changeHostId"
          :placeholder="$t('enterprise.NodeConfig.5mpme7w6b3k0')" @popup-visible-change="hostPopupChange" class="mr-s">
          <a-option v-for="item in data.hostList" :key="item.hostId" :value="item.hostId">{{
            item.privateIp
            + '(' +
            (item.publicIp ? item.publicIp : '--') + ')'
          }}</a-option>
        </a-select>
        <icon-code-square :size="25" class="label-color" style="cursor: pointer;" @click="showTerminal" />
      </a-form-item>
      <a-form-item v-if="form.isNeedPwd" field="rootPassword" :label="$t('enterprise.NodeConfig.else2')"
        validate-trigger="blur">
        <a-input-password v-model="form.rootPassword" :placeholder="$t('enterprise.NodeConfig.5mpme7w6b700')"
          allow-clear />
      </a-form-item>
      <a-form-item field="installUserId" :label="$t('enterprise.NodeConfig.5mpme7w6bak0')">
        <a-select :loading="data.installUserLoading" v-model="form.installUserId" @change="changeInstallUserId"
          @popup-visible-change="hostUserPopupChange">
          <a-option v-for="item in data.userListByHost[form.hostId]" :key="item.hostUserId" :value="item.hostUserId">{{
            item.username
          }}</a-option>
        </a-select>
      </a-form-item>
      <a-row :gutter="24">
        <a-col :span="12">
          <a-form-item v-if="clusterData.isInstallCM" field="isCMMaster"
            :label="$t('enterprise.NodeConfig.5mpme7w6be40')">
            <a-switch v-model="form.isCMMaster" @change="handleNodeCMChange" />
          </a-form-item>
        </a-col>
      </a-row>
      <a-form-item v-if="clusterData.isInstallCM" field="cmDataPath" :label="$t('enterprise.NodeConfig.else4')"
        validate-trigger="blur">
        <a-input v-model.trim="form.cmDataPath" :placeholder="$t('enterprise.NodeConfig.5mpme7w6bhg0')" />
      </a-form-item>
      <a-form-item v-if="clusterData.isInstallCM" field="cmPort" :label="$t('enterprise.NodeConfig.else5')"
        validate-trigger="blur">
        <a-input v-model.trim="form.cmPort" :placeholder="$t('enterprise.NodeConfig.5mpme7w6bko0')" />
      </a-form-item>
      <div class="label-color ft-m ft-b mb">
        {{ $t('enterprise.NodeConfig.5mpme7w6boc0') }}
      </div>
      <a-form-item field="dataPath" :label="$t('enterprise.NodeConfig.5mpme7w6brs0')" validate-trigger="blur">
        <a-input v-model.trim="form.dataPath" :placeholder="$t('enterprise.NodeConfig.5mpme7w6bv40')" />
      </a-form-item>
      <a-form-item field="azPriority" :label="$t('enterprise.NodeConfig.else7')" v-if="installType !== 'import'">
        <a-input-number :min="1" :max="10" v-model="form.azPriority" :placeholder="$t('enterprise.NodeConfig.else6')" />
      </a-form-item>
    </a-form>
    <host-terminal ref="hostTerminalRef"></host-terminal>
  </div>
</template>

<script setup lang="ts">
import { KeyValue } from '@/types/global'
import { useOpsStore } from '@/store'
import { FormInstance } from '@arco-design/web-vue/es/form'
import { PropType, ref, computed, defineProps, inject, reactive, onMounted } from 'vue'
import { Message } from '@arco-design/web-vue'
import { hostListAll, hostUserListWithoutRoot, portUsed, pathEmpty, fileExist, hostPingById } from '@/api/ops'
import HostTerminal from "@/views/ops/install/components/hostTerminal/HostTerminal.vue";
import { encryptPassword } from '@/utils/jsencrypt'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()

const installStore = useOpsStore()
const installType = computed(() => installStore.getInstallConfig.installType)

const cmUtilFunc = inject<any>('cmUtil')

const props = defineProps({
  formData: {
    type: Object as PropType<KeyValue>,
    required: true
  },
  clusterData: {
    type: Object as PropType<KeyValue>,
    default: true
  }
})

onMounted(() => {
  getHostList()
})

const emits = defineEmits([`update:formData`])
const form = computed({
  get: () => props.formData,
  set: (val) => {
    emits(`update:formData`, val)
  }
})

const data = reactive<KeyValue>({
  hostListLoading: false,
  hostList: [],
  hostObj: {},
  installUserLoading: false,
  userListByHost: {},
  installUserObj: {}
})

const formRules = computed(() => {
  return {
    hostId: [{ required: true, 'validate-trigger': 'change', message: t('enterprise.NodeConfig.5mpme7w6c1w0') }],
    rootPassword: [
      { required: true, 'validate-trigger': 'blur', message: t('enterprise.NodeConfig.5mpme7w6b700') },
      {
        validator: (value: any, cb: any) => {
          return new Promise(resolve => {
            if (!value.trim()) {
              cb(t('enterprise.ClusterConfig.else2'))
              resolve(false)
            } else {
              resolve(true)
            }
          })
        }
      }
    ],
    installUserId: [{ required: true, 'validate-trigger': 'change', message: t('enterprise.NodeConfig.5mpme7w6c5g0') }],
    cmPort: [
      { required: true, 'validate-trigger': 'blur', message: t('enterprise.NodeConfig.5mpme7w6bko0') },
      {
        validator: (value: any, cb: any) => {
          return new Promise(resolve => {
            if (!value.trim()) {
              cb(t('enterprise.ClusterConfig.else2'))
              resolve(false)
            }
            const reg = /^([0-9]|[1-9]\d{1,3}|[1-5]\d{4}|6[0-4]\d{4}|65[0-4]\d{2}|655[0-2]\d|6553[0-5])$/
            const re = new RegExp(reg)
            if (re.test(value)) {
              resolve(true)
            } else {
              cb(t('simple.InstallConfig.else2'))
              resolve(false)
            }
          })
        }
      }
    ],
    cmDataPath: [
      { required: true, 'validate-trigger': 'blur', message: t('enterprise.NodeConfig.5mpme7w6c8s0') },
      {
        validator: (value: any, cb: any) => {
          return new Promise(resolve => {
            if (!value.trim()) {
              cb(t('enterprise.ClusterConfig.else2'))
              resolve(false)
            } else {
              resolve(true)
            }
          })
        }
      }
    ],
    dataPath: [
      { required: true, 'validate-trigger': 'blur', message: t('enterprise.NodeConfig.5mpme7w6cc40') },
      {
        validator: (value: any, cb: any) => {
          return new Promise(resolve => {
            if (!value.trim()) {
              cb(t('enterprise.ClusterConfig.else2'))
              resolve(false)
            } else {
              resolve(true)
            }
          })
        }
      }
    ],
    azPriority: [{ required: true, 'validate-trigger': 'blur', message: t('enterprise.NodeConfig.else6') }]
  }
})

// methods
const getHostList = () => {
  data.hostListLoading = true
  hostListAll().then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      data.hostList = []
      data.hostList = res.data
      res.data.forEach((item: KeyValue) => {
        data.hostObj[item.hostId] = item
      })
      changeHostId()
    } else {
      Message.error('Failed to obtain the host list data')
    }
  }).finally(() => {
    data.hostListLoading = false
  })
}

const changeHostId = () => {
  const hostId = form.value.hostId
  if (hostId) {
    if (data.hostObj[hostId]) {
      form.value.privateIp = data.hostObj[hostId].privateIp
      form.value.publicIp = data.hostObj[hostId].publicIp
      form.value.hostname = data.hostObj[hostId].hostname
      form.value.isNeedPwd = !data.hostObj[hostId].isRemember
    }
    if (data.userListByHost[hostId] && !form.value.installUserId) {
      form.value.installUserId = data.userListByHost[hostId][0].hostUserId
      form.value.installUsername = data.userListByHost[hostId][0].username
    } else {
      data.installUserLoading = true
      hostUserListWithoutRoot(hostId).then((res: KeyValue) => {
        if (Number(res.code) === 200) {
          if (res.data.length) {
            data.userListByHost[hostId] = res.data
            res.data.forEach((installUser: KeyValue) => {
              data.installUserObj[installUser.hostUserId] = installUser
            })
            const hostUserId = form.value.installUserId
            if (hostUserId) {
              const hasExist = res.data.find((item: KeyValue) => {
                return item.hostUserId === hostUserId
              })
              if (!hasExist) {
                form.value.installUserId = data.userListByHost[hostId][0].hostUserId
                form.value.installUsername = data.userListByHost[hostId][0].username
              }
            } else {
              form.value.installUserId = data.userListByHost[hostId][0].hostUserId
              form.value.installUsername = data.userListByHost[hostId][0].username
            }
          } else {
            form.value.installUserId = ''
            form.value.installUsername = ''
          }
          formRef.value?.validateField('installUserId')
        } else {
          Message.error('Failed to obtain user data from the host')
        }
      }).finally(() => {
        data.installUserLoading = false
      })
    }
  }
}

const hostPopupChange = (val: boolean) => {
  if (val) {
    getHostList()
  }
}

const hostUserPopupChange = (val: boolean) => {
  if (val) {
    changeHostId()
  }
}

const changeInstallUserId = (installUserId: any) => {
  const getInstallUser = data.installUserObj[installUserId]
  if (getInstallUser) {
    form.value.installUsername = getInstallUser.username
  }
}

const handleNodeCMChange = (val: any) => {
  cmUtilFunc.isHasCMMaster(form.value.id, val)
}

const formRef = ref<null | FormInstance>(null)

const hostTerminalRef = ref<null | InstanceType<typeof HostTerminal>>(null)
const showTerminal = () => {
  // isRemember password
  if (form.value.isNeedPwd) {
    if (!form.value.rootPassword) {
      formRef.value?.setFields({
        rootPassword: {
          status: 'error',
          message: t('simple.InstallConfig.5mpmu0laqwo0')
        }
      })
      return
    }
  }
  if (!form.value.hostId) {
    formRef.value?.setFields({
      hostId: {
        status: 'error',
        message: t('lightweight.InstallConfig.5mpmkfqybw00')
      }
    })
    return
  }
  // showTerminal
  handleShowTerminal({
    hostId: form.value.hostId,
    port: form.value.port,
    ip: form.value.publicIp,
    password: form.value.rootPassword
  })
}

const handleShowTerminal = (data: KeyValue) => {
  hostTerminalRef.value?.open(data)
}

const validatePort = async (port: number, password: string, hostId: string): Promise<any> => {
  const portParam = {
    port: port,
    rootPassword: password
  }
  const portValid: KeyValue = await portUsed(hostId, portParam)
  if (Number(portValid.code) === 200) {
    return portValid.data
  }
  return false
}

const validatePath = async (path: string, password: string, hostId: string) => {
  const pathParam = {
    path: path,
    rootPassword: password
  }
  const pathValid: KeyValue = await pathEmpty(hostId, pathParam)
  if (Number(pathValid.code) === 200) {
    return pathValid.data
  }
  return false
}

const validateFile = async (file: string, password: string, hostId: string) => {
  const pathParam = {
    file: file,
    rootPassword: password
  }
  const pathValid: KeyValue = await fileExist(hostId, pathParam)
  if (Number(pathValid.code) === 200) {
    return pathValid.data
  }
  return false
}

const validateSpecialFields = async () => {
  let result = true

  console.log('校验密码', form.value);

  formRef.value?.clearValidate()

  const validMethodArr = []
  let isOkPwd = true
  let encryptPwd = ''
  if (form.value.rootPassword) {
    encryptPwd = await encryptPassword(form.value.rootPassword)
  }
  // password validate
  try {
    const param = {
      rootPassword: encryptPwd
    }
    const passwordValid: KeyValue = await hostPingById(form.value.hostId, param)
    if (Number(passwordValid.code) !== 200) {
      formRef.value?.setFields({
        rootPassword: {
          status: 'error',
          message: t('enterprise.NodeConfig.else8')
        }
      })
      result = false
      isOkPwd = false
    }
  } catch (err: any) {
    formRef.value?.setFields({
      rootPassword: {
        status: 'error',
        message: t('enterprise.NodeConfig.else9')
      }
    })
    result = false
    isOkPwd = false
  }
  if (!isOkPwd) {
    return result
  }
  //  cluster port is used
  validMethodArr.push(validatePort(props.clusterData.port, encryptPwd, form.value.hostId))
  validMethodArr.push(validatePath(form.value.dataPath, encryptPwd, form.value.hostId))
  validMethodArr.push(validatePath(props.clusterData.installPackagePath, encryptPwd, form.value.hostId))
  if (props.clusterData.isInstallCM) {
    validMethodArr.push(validatePort(form.value.cmPort, encryptPwd, form.value.hostId))
  }
  if (props.clusterData.envPath && installType.value === 'import') {
    validMethodArr.push(validateFile(props.clusterData.envPath, encryptPwd, form.value.hostId))
  }
  if (validMethodArr.length) {
    const validResult = await Promise.all(validMethodArr)
    if ((installType.value !== 'import' && validResult[0]) || (installType.value === 'import' && !validResult[0])) {
      // port valid
      formRef.value?.setFields({
        hostId: {
          status: 'error',
          message: props.clusterData.port + (installType.value === 'import' ? t('enterprise.NodeConfig.else10') : t('enterprise.NodeConfig.else11'))
        }
      })
      result = false
    }
    if ((installType.value !== 'import' && !validResult[1]) || (installType.value === 'import' && validResult[1])) {
      // dataPath valid
      formRef.value?.setFields({
        dataPath: {
          status: 'error',
          message: installType.value === 'import' ? t('enterprise.NodeConfig.else12') : t('enterprise.NodeConfig.else13')
        }
      })
      result = false
    }

    if ((installType.value !== 'import' && !validResult[2]) || (installType.value === 'import' && validResult[2])) {
      // installPackagePath valid
      formRef.value?.setFields({
        hostId: {
          status: 'error',
          message: (installType.value === 'import' ? t('enterprise.NodeConfig.else14') : t('enterprise.NodeConfig.else16')) + ',' + props.clusterData.installPackagePath
        }
      })
      result = false
    }
    if (props.clusterData.isInstallCM) {
      if ((installType.value !== 'import' && validResult[3]) || (installType.value === 'import' && !validResult[3])) {
        // cmPort valid
        formRef.value?.setFields({
          cmPort: {
            status: 'error',
            message: form.value.cmPort + (installType.value === 'import' ? t('enterprise.NodeConfig.else10') : t('enterprise.NodeConfig.else11'))
          }
        })
        result = false
      }
      if (installType.value === 'import' && props.clusterData.envPath && !validResult[4]) {
        // envPath valid
        formRef.value?.setFields({
          hostId: {
            status: 'error',
            message: t('enterprise.NodeConfig.else17') + ',' + props.clusterData.envPath
          }
        })
        result = false
      }
    } else {
      if (installType.value === 'import' && props.clusterData.envPath && !validResult[3]) {
        // envPath valid
        formRef.value?.setFields({
          hostId: {
            status: 'error',
            message: t('enterprise.NodeConfig.else17') + ',' + props.clusterData.envPath
          }
        })
        result = false
      }
    }

  }
  return result
}

const formValidate = async (): Promise<KeyValue> => {
  let validResult = false
  const validRes = await formRef.value?.validate()
  if (!validRes) {
    // valid info
    validResult = true
  }
  const result = {
    id: form.value.id,
    res: validResult
  }
  return result
}

const dataValidate = async (): Promise<KeyValue> => {
  let validResult = false
  // valid info
  validResult = await validateSpecialFields()
  const result = {
    id: form.value.id,
    res: validResult
  }
  return result
}

defineExpose({
  formValidate,
  dataValidate
})


</script>
<style lang="less" scoped>
.node-form-c {
  padding: 20px;
}
</style>