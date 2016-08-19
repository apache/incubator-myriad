package org.apache.myriad.state;

import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentMap;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.ha.HAServiceProtocol;
import org.apache.hadoop.ha.HAServiceProtocol.HAServiceState;
import org.apache.hadoop.yarn.api.records.ApplicationId;
import org.apache.hadoop.yarn.api.records.NodeId;
import org.apache.hadoop.yarn.conf.ConfigurationProvider;
import org.apache.hadoop.yarn.event.Dispatcher;
import org.apache.hadoop.yarn.server.resourcemanager.AdminService;
import org.apache.hadoop.yarn.server.resourcemanager.ApplicationMasterService;
import org.apache.hadoop.yarn.server.resourcemanager.ClientRMService;
import org.apache.hadoop.yarn.server.resourcemanager.NodesListManager;
import org.apache.hadoop.yarn.server.resourcemanager.RMActiveServiceContext;
import org.apache.hadoop.yarn.server.resourcemanager.RMContext;
import org.apache.hadoop.yarn.server.resourcemanager.ResourceTrackerService;
import org.apache.hadoop.yarn.server.resourcemanager.ahs.RMApplicationHistoryWriter;
import org.apache.hadoop.yarn.server.resourcemanager.metrics.SystemMetricsPublisher;
import org.apache.hadoop.yarn.server.resourcemanager.nodelabels.RMNodeLabelsManager;
import org.apache.hadoop.yarn.server.resourcemanager.recovery.RMStateStore;
import org.apache.hadoop.yarn.server.resourcemanager.reservation.ReservationSystem;
import org.apache.hadoop.yarn.server.resourcemanager.rmapp.RMApp;
import org.apache.hadoop.yarn.server.resourcemanager.rmapp.attempt.AMLivelinessMonitor;
import org.apache.hadoop.yarn.server.resourcemanager.rmcontainer.ContainerAllocationExpirer;
import org.apache.hadoop.yarn.server.resourcemanager.rmnode.RMNode;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.ResourceScheduler;
import org.apache.hadoop.yarn.server.resourcemanager.security.AMRMTokenSecretManager;
import org.apache.hadoop.yarn.server.resourcemanager.security.ClientToAMTokenSecretManagerInRM;
import org.apache.hadoop.yarn.server.resourcemanager.security.DelegationTokenRenewer;
import org.apache.hadoop.yarn.server.resourcemanager.security.NMTokenSecretManagerInRM;
import org.apache.hadoop.yarn.server.resourcemanager.security.RMContainerTokenSecretManager;
import org.apache.hadoop.yarn.server.resourcemanager.security.RMDelegationTokenSecretManager;

/**
 * Mock implementation of RMContext for the purposes of JUnit tests
 */
public class MockRMContext implements RMContext {
  Dispatcher dispatcher;
  boolean haEnabled = false;
  RMStateStore stateStore;
  AMLivelinessMonitor amLivelinessMonitor;
  AMLivelinessMonitor amFinishingMonitor;
  RMActiveServiceContext activeServiceContext;
  HAServiceState haServiceState = HAServiceProtocol.HAServiceState.INITIALIZING;
  Configuration yarnConfiguration;
  RMNodeLabelsManager mgr;
  ResourceScheduler resourceScheduler;
  boolean workPreservingRecoveryEnabled;
  NMTokenSecretManagerInRM nmTokenSecretManager;
  RMApplicationHistoryWriter rmApplicationHistoryWriter = new RMApplicationHistoryWriter();
  RMDelegationTokenSecretManager delegationTokenSecretManager;
  RMContainerTokenSecretManager containerTokenSecretManager;
  NodesListManager nodesListManager;
  ContainerAllocationExpirer containerAllocationExpirer;
  AMRMTokenSecretManager tokenSecretManager;
  DelegationTokenRenewer delegationTokenRenewer;
  ClientRMService clientRMService;
  ApplicationMasterService applicationMasterService;
  ResourceTrackerService resourceTrackerService;
  SystemMetricsPublisher systemMetricsPublisher;
  ConfigurationProvider configurationProvider;
  AdminService adminService;
  ConcurrentMap<NodeId, RMNode> rmNodes;
  
  public void setApplicationMasterService(ApplicationMasterService applicationMasterService) {
    this.applicationMasterService = applicationMasterService;
  }

  public RMActiveServiceContext getActiveServiceContext() {
    return activeServiceContext;
  }

  public void setActiveServiceContext(RMActiveServiceContext activeServiceContext) {
    this.activeServiceContext = activeServiceContext;
  }

  public void setResourceTrackerService(ResourceTrackerService resourceTrackerService) {
    this.resourceTrackerService = resourceTrackerService;
  }

  public void setContainerTokenSecretManager(RMContainerTokenSecretManager containerTokenSecretManager) {
    this.containerTokenSecretManager = containerTokenSecretManager;
  }

  public void setDelegationTokenRenewer(DelegationTokenRenewer delegationTokenRenewer) {
    this.delegationTokenRenewer = delegationTokenRenewer;
  }

  public void setAdminService(AdminService adminService) {
    this.adminService = adminService;
  }

  public void setConfigurationProvider(ConfigurationProvider configurationProvider) {
    this.configurationProvider = configurationProvider;
  }

  public void setDelegationTokenSecretManager(RMDelegationTokenSecretManager delegationTokenSecretManager) {
    this.delegationTokenSecretManager = delegationTokenSecretManager;
  }

  public void setTokenSecretManager(AMRMTokenSecretManager tokenSecretManager) {
    this.tokenSecretManager = tokenSecretManager;
  }

  public void setContainerAllocationExpirer(ContainerAllocationExpirer containerAllocationExpirer) {
    this.containerAllocationExpirer = containerAllocationExpirer;
  }

  public void setNodesListManager(NodesListManager nodesListManager) {
    this.nodesListManager = nodesListManager;
  }

  public void setNmTokenSecretManager(NMTokenSecretManagerInRM nmTokenSecretManager) {
    this.nmTokenSecretManager = nmTokenSecretManager;
  }

  public void setWorkPreservingRecoveryEnabled(boolean workPreservingRecoveryEnabled) {
    this.workPreservingRecoveryEnabled = workPreservingRecoveryEnabled;
  }

  public void setResourceScheduler(ResourceScheduler resourceScheduler) {
    this.resourceScheduler = resourceScheduler;
  }

  public void setMgr(RMNodeLabelsManager mgr) {
    this.mgr = mgr;
  }

  public void setYarnConfiguration(Configuration yarnConfiguration) {
    this.yarnConfiguration = yarnConfiguration;
  }
  
  public void setHaEnabled(boolean haEnabled) {
    this.haEnabled = haEnabled;
  }

  public void setAmLivelinessMonitor(AMLivelinessMonitor amLivelinessMonitor) {
    this.amLivelinessMonitor = amLivelinessMonitor;
  }

  public void setAmFinishingMonitor(AMLivelinessMonitor amFinishingMonitor) {
    this.amFinishingMonitor = amFinishingMonitor;
  }

  public void setDispatcher(Dispatcher dispatcher) {
    this.dispatcher = dispatcher;
  }

  public void setStateStore(RMStateStore stateStore) {
    this.stateStore = stateStore;
  }

  @Override
  public Dispatcher getDispatcher() {
    return dispatcher;
  }

  @Override
  public boolean isHAEnabled() {
    return haEnabled;
  }

  @Override
  public HAServiceState getHAServiceState() {
    return haServiceState;
  }

  @Override
  public RMStateStore getStateStore() {
    return stateStore;
  }

  @Override
  public ConcurrentMap<ApplicationId, RMApp> getRMApps() {
    return null;
  }

  @Override
  public ConcurrentMap<ApplicationId, ByteBuffer> getSystemCredentialsForApps() {
    return null;
  }

  @Override
  public ConcurrentMap<String, RMNode> getInactiveRMNodes() {
    return null;
  }

  @Override
  public ConcurrentMap<NodeId, RMNode> getRMNodes() {
    return this.rmNodes;
  }

  public void setRMNodes(ConcurrentMap<NodeId, RMNode> rmNodes) {
    this.rmNodes = rmNodes;
  }
  @Override
  public AMLivelinessMonitor getAMLivelinessMonitor() {
    return amLivelinessMonitor;
  }

  @Override
  public AMLivelinessMonitor getAMFinishingMonitor() {
    return amFinishingMonitor;
  }

  @Override
  public ContainerAllocationExpirer getContainerAllocationExpirer() {
    return containerAllocationExpirer;
  }

  @Override
  public DelegationTokenRenewer getDelegationTokenRenewer() {
    return delegationTokenRenewer;
  }

  @Override
  public AMRMTokenSecretManager getAMRMTokenSecretManager() {
    return tokenSecretManager;
  }

  @Override
  public RMContainerTokenSecretManager getContainerTokenSecretManager() {
    return containerTokenSecretManager;
  }

  @Override
  public NMTokenSecretManagerInRM getNMTokenSecretManager() {
    return nmTokenSecretManager;
  }

  @Override
  public ResourceScheduler getScheduler() {
    return resourceScheduler;
  }

  @Override
  public NodesListManager getNodesListManager() {
    return nodesListManager;
  }

  @Override
  public ClientToAMTokenSecretManagerInRM getClientToAMTokenSecretManager() {
    return null;
  }

  @Override
  public AdminService getRMAdminService() {
    return adminService;
  }

  @Override
  public ClientRMService getClientRMService() {
    return clientRMService;
  }

  @Override
  public ApplicationMasterService getApplicationMasterService() {
    return applicationMasterService;
  }

  @Override
  public ResourceTrackerService getResourceTrackerService() {
    return resourceTrackerService;
  }

  @Override
  public void setClientRMService(ClientRMService clientRMService) {
    this.clientRMService = clientRMService;
  }

  @Override
  public RMDelegationTokenSecretManager getRMDelegationTokenSecretManager() {
    return delegationTokenSecretManager;
  }

  @Override
  public void setRMDelegationTokenSecretManager(RMDelegationTokenSecretManager delegationTokenSecretManager) {
    this.delegationTokenSecretManager = delegationTokenSecretManager;
  }

  @Override
  public RMApplicationHistoryWriter getRMApplicationHistoryWriter() {
    return rmApplicationHistoryWriter;
  }

  @Override
  public void setRMApplicationHistoryWriter(RMApplicationHistoryWriter rmApplicationHistoryWriter) {
    this.rmApplicationHistoryWriter = rmApplicationHistoryWriter;
  }

  @Override
  public void setSystemMetricsPublisher(SystemMetricsPublisher systemMetricsPublisher) {
    this.systemMetricsPublisher = systemMetricsPublisher;
  }

  @Override
  public SystemMetricsPublisher getSystemMetricsPublisher() {
    return systemMetricsPublisher;
  }

  @Override
  public ConfigurationProvider getConfigurationProvider() {
    return configurationProvider;
  }

  @Override
  public boolean isWorkPreservingRecoveryEnabled() {
    return workPreservingRecoveryEnabled;
  }

  @Override
  public RMNodeLabelsManager getNodeLabelManager() {
    return mgr;
  }

  @Override
  public void setNodeLabelManager(RMNodeLabelsManager mgr) {
    this.mgr = mgr;
  }

  @Override
  public long getEpoch() {
    return 0;
  }

  @Override
  public ReservationSystem getReservationSystem() {
    return null;
  }

  @Override
  public boolean isSchedulerReadyForAllocatingContainers() {
    return false;
  }

  @Override
  public Configuration getYarnConfiguration() {
    return this.yarnConfiguration;
  }
}