public class ConstrutionEventTaskImpl implements IConstrutionEventTaskService {
    @Override
    public void updateStatusEventTaskByRectify(RectifyOrderEntity rectifyOrder, ConstructionEventTaskStatusEnum constructionEventTaskStatusEnum) {
        ThreadPoolUtil.getOrderThreadPool().submit(() -> {
            Integer status = constructionEventTaskStatusEnum.getCode();
            if (DeleteEnum.DELETE.getState().equals(rectifyOrder.getIsDel())) {
                status = ConstructionEventTaskStatusEnum.CANCEL.getCode();
            }

            // 1111111
            ZrspTaskListenerMessage zrspTaskListenerMessage = new ZrspTaskListenerMessage();
            zrspTaskListenerMessage.setEventType(ConstructionEventTaskTypeEnum.ZG.getCode());
            zrspTaskListenerMessage.setTaskBusinessSign(rectifyOrder.getId().toString());
            zrspTaskListenerMessage.setTaskStatus(status);
            zrspTaskListenerMessage.setVersion(System.currentTimeMillis());
            log.info("1111111{}", rectifyOrder.getId());
            this.sendMqToUpdateEventTask(zrspTaskListenerMessage);
        });
            // 1111111
            List<VenderConstructDetailEntity> zxListByOrderCode = iVenderConstructDetailService.findZXListByOrderCode(orderCode);
            if (CollectionUtils.isEmpty(zxListByOrderCode)) {
                return;
            }
            zxListByOrderCode = iConstructDetailService.fillCheckTypeFields(zxListByOrderCode);
            Map<Integer, List<VenderConstructDetailEntity>> checkTypeMap = zxListByOrderCode.stream().filter(x -> Objects.nonNull(x.getCheckType())).collect(Collectors.groupingBy(VenderConstructDetailEntity::getCheckType));
            checkTypeMap.forEach((k, v) -> {
                List<VenderConstructDetailEntity> noAccepConstructDetailList = Optional.ofNullable(v).orElse(new ArrayList<>()).stream().filter(x -> x.getConstructDetailStatus() < VenderOrderDetailConst.VenderOrderDetailStatus.ALREADYACCEPT.getStatusCode()).collect(Collectors.toList());
                if (CollectionUtils.isEmpty(noAccepConstructDetailList)) {
                    String taskBusinessSign = orderCode + "_" + k;
                    log.info("1111112221{}, status{}", taskBusinessSign, ConstructionEventTaskStatusEnum.COMPLETED.getCode());
                    updateEventTaskStatus(ConstructionEventTaskTypeEnum.JDJS.getCode(), orderCode, taskBusinessSign, ConstructionEventTaskStatusEnum.COMPLETED.getCode());
                }
            });
    }

    @Override
    public void updateAcceptTaskByCheckZero(VenderConstructDetailEntity constructDetail) {
        ThreadPoolUtil.getOrderThreadPool().submit(() -> {
            if (isCheckSupplierType(constructDetail.getConstructType())) {
                return;
            }
            String orderCode = constructDetail.getOrderCode();
            // 1111111
            String taskBusinessSign = String.valueOf(constructDetail.getId());
            log.info("1111111{}, status{}", taskBusinessSign, ConstructionEventTaskStatusEnum.COMPLETED.getCode());
            updateEventTaskStatus(ConstructionEventTaskTypeEnum.TJJG.getCode(), orderCode, taskBusinessSign, ConstructionEventTaskStatusEnum.COMPLETED.getCode());
        });
    }
}
