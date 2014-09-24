package coffee.letsgo.columbus.service.model;

public enum ServiceStatus implements ServiceState {
    UNINITIALIZED {
        @Override
        public void process(ServiceContext ctx, ServiceEvent evt) {
            switch (evt) {
                case UPDATING_ALL:
                    ctx.setState(INITIALIZING);
                    break;
                case UPDATED_ALL:
                    ctx.setState(INITIALIZED);
                default:
                    break;
            }
        }
    },
    INITIALIZING {
        @Override
        public void process(ServiceContext ctx, ServiceEvent evt) {
            switch (evt) {
                case DISCONNECTED:
                case EXPIRED:
                case UPDATE_FAILED:
                    ctx.setState(UNINITIALIZED);
                    break;
                case UPDATED_ALL:
                    ctx.setState(INITIALIZED);
                    break;
                default:
                    break;
            }
        }
    },
    INITIALIZED {
        @Override
        public void process(ServiceContext ctx, ServiceEvent evt) {
            switch (evt) {
                case DISCONNECTED:
                case EXPIRED:
                case UPDATE_FAILED:
                case MEMBER_CHANGED:
                case ACTIVE_CHANGED:
                    ctx.setState(UNSYNCED);
                    break;
                case UPDATING:
                case UPDATING_ALL:
                    ctx.setState(SYNCING);
                    break;
                case UPDATED:
                case UPDATED_ALL:
                    ctx.setState(SYNCED);
                    break;
                default:
                    break;
            }
        }
    },
    UNSYNCED {
        @Override
        public void process(ServiceContext ctx, ServiceEvent evt) {
            switch (evt) {
                case UPDATING:
                case UPDATING_ALL:
                    ctx.setState(SYNCING);
                    break;
                case UPDATED:
                case UPDATED_ALL:
                    ctx.setState(SYNCED);
                    break;
                default:
                    break;
            }
        }
    },
    SYNCING {
        @Override
        public void process(ServiceContext ctx, ServiceEvent evt) {
            switch (evt) {
                case DISCONNECTED:
                case EXPIRED:
                case UPDATE_FAILED:
                    ctx.setState(UNSYNCED);
                    break;
                case UPDATED:
                case UPDATED_ALL:
                    ctx.setState(SYNCED);
                    break;
                default:
                    break;
            }
        }
    },
    SYNCED {
        @Override
        public void process(ServiceContext ctx, ServiceEvent evt) {
            switch (evt) {
                case DISCONNECTED:
                case EXPIRED:
                case UPDATE_FAILED:
                case MEMBER_CHANGED:
                case ACTIVE_CHANGED:
                    ctx.setState(UNSYNCED);
                    break;
                case UPDATING:
                case UPDATING_ALL:
                    ctx.setState(SYNCING);
                    break;
                default:
                    break;
            }
        }
    }
}
