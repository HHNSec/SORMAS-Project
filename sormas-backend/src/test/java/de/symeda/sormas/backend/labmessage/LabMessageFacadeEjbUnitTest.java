package de.symeda.sormas.backend.labmessage;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Root;

import de.symeda.sormas.backend.systemevent.sync.SyncFacadeEjb;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import de.symeda.sormas.api.labmessage.LabMessageCriteria;
import de.symeda.sormas.api.labmessage.LabMessageDto;
import de.symeda.sormas.api.labmessage.LabMessageIndexDto;
import de.symeda.sormas.api.systemevents.SystemEventDto;
import de.symeda.sormas.api.systemevents.SystemEventStatus;
import de.symeda.sormas.api.systemevents.SystemEventType;
import de.symeda.sormas.api.utils.SortProperty;
import de.symeda.sormas.backend.systemevent.SystemEventFacadeEjb;

@RunWith(MockitoJUnitRunner.class)
public class LabMessageFacadeEjbUnitTest {

	@Mock
	private EntityManager em;
	@Mock
	private LabMessageService labMessageService;
	@Mock
	private SystemEventFacadeEjb.SystemEventFacadeEjbLocal systemEventFacade;

	@InjectMocks
	private LabMessageFacadeEjb sut;

	@InjectMocks
	private SyncFacadeEjb.SyncFacadeEjbLocal syncFacadeEjb;

	@Mock
	private CriteriaBuilder criteriaBuilder;
	@Mock
	private CriteriaQuery<LabMessageIndexDto> labMessageIndexDtoCriteriaQuery;
	@Mock
	private CriteriaQuery<Long> longCriteriaQuery;
	@Mock
	private Root<LabMessage> labMessage;
	@Mock
	private TypedQuery<LabMessageIndexDto> labMessageIndexDtoTypedQuery;
	@Mock
	Join<Object, Object> userJoin;
	@Mock
	private TypedQuery<Long> longTypedQuery;
	@Mock
	private Expression<Long> longExpression;
	@Captor
	private ArgumentCaptor<List<Order>> orderListArgumentCaptor;

	@Test
	public void testCount() {

		when(em.getCriteriaBuilder()).thenReturn(criteriaBuilder);
		when(criteriaBuilder.createQuery(Long.class)).thenReturn(longCriteriaQuery);
		when(longCriteriaQuery.from(LabMessage.class)).thenReturn(labMessage);
		when(criteriaBuilder.countDistinct(labMessage)).thenReturn(longExpression);
		when(em.createQuery(longCriteriaQuery)).thenReturn(longTypedQuery);
		long expected = 1L;
		when(longTypedQuery.getSingleResult()).thenReturn(expected);

		long result = sut.count(new LabMessageCriteria());
		assertEquals(expected, result);
	}

	@Test
	public void testGetIndexList() {

		int first = 1;
		int max = 1;

		when(em.getCriteriaBuilder()).thenReturn(criteriaBuilder);
		when(criteriaBuilder.createQuery(LabMessageIndexDto.class)).thenReturn(labMessageIndexDtoCriteriaQuery);
		when(labMessageIndexDtoCriteriaQuery.from(LabMessage.class)).thenReturn(labMessage);
		when(em.createQuery(labMessageIndexDtoCriteriaQuery)).thenReturn(labMessageIndexDtoTypedQuery);
		when(labMessageIndexDtoTypedQuery.setFirstResult(first)).thenReturn(labMessageIndexDtoTypedQuery);
		when(labMessageIndexDtoTypedQuery.setMaxResults(max)).thenReturn(labMessageIndexDtoTypedQuery);
		ArrayList<LabMessageIndexDto> expectedResult = new ArrayList<>();
		when(labMessageIndexDtoTypedQuery.getResultList()).thenReturn(expectedResult);
		when(labMessage.join(LabMessage.ASSIGNEE, JoinType.LEFT)).thenReturn(userJoin);
		when(userJoin.get((String) any())).thenReturn(null);
		ArrayList<SortProperty> sortProperties = new ArrayList<>();
		sortProperties.add(new SortProperty(LabMessageIndexDto.UUID));
		sortProperties.add(new SortProperty("No Valid Property"));
		List<LabMessageIndexDto> result = sut.getIndexList(new LabMessageCriteria(), first, max, sortProperties);

		verify(labMessageIndexDtoCriteriaQuery).orderBy(orderListArgumentCaptor.capture());
		assertEquals(2, orderListArgumentCaptor.getValue().size());
		assertEquals(expectedResult, result);
	}

	@Test
	public void testSave() {

		LabMessageDto labMessageDto = new LabMessageDto();
		String testUuid = "Test UUID";
		labMessageDto.setUuid(testUuid);
		LabMessage labMessage = new LabMessage();

		when(labMessageService.getByUuid(testUuid)).thenReturn(labMessage);
		sut.save(labMessageDto);

		verify(labMessageService).ensurePersisted(labMessage);
	}

	@Test
	public void testGetByUuid() {

		String testUuid = "test UUID";
		LabMessage labMessage = new LabMessage();
		when(labMessageService.getByUuid(testUuid)).thenReturn(labMessage);
		LabMessageDto result = sut.getByUuid(testUuid);
		assertEquals(sut.toDto(labMessage), result);
	}

	@Test
	public void testInitializeUpdateDateWithNoPreviousSuccess() {
		assertEquals(syncFacadeEjb.findLastSyncDateFor(SystemEventType.FETCH_LAB_MESSAGES), new Date(0));
	}

	@Test
	public void testInitializeUpdateDateWithPreviousSuccessAndParseableDetails() {

		SystemEventDto systemEvent = SystemEventDto.build();
		Date first = new Date(100, 0, 1);
		Date second = new Date(100, 0, 2);
		systemEvent.setStatus(SystemEventStatus.SUCCESS);
		systemEvent.setType(SystemEventType.FETCH_LAB_MESSAGES);
		systemEvent.setAdditionalInfo("Last synchronization date: " + first.getTime());
		systemEvent.setStartDate(second);
		when(systemEventFacade.getLatestSuccessByType(SystemEventType.FETCH_LAB_MESSAGES)).thenReturn(systemEvent);
		assertEquals(syncFacadeEjb.findLastSyncDateFor(SystemEventType.FETCH_LAB_MESSAGES), first);
	}

	@Test
	public void testInitializeUpdateDateWithPreviousSuccessAndNotParseableDetails() {

		SystemEventDto systemEvent = SystemEventDto.build();
		Date date = new Date(100, 0, 1);
		systemEvent.setStatus(SystemEventStatus.SUCCESS);
		systemEvent.setType(SystemEventType.FETCH_LAB_MESSAGES);
		systemEvent.setAdditionalInfo("The cake is a lie");
		systemEvent.setStartDate(date);
		when(systemEventFacade.getLatestSuccessByType(SystemEventType.FETCH_LAB_MESSAGES)).thenReturn(systemEvent);
		assertEquals(syncFacadeEjb.findLastSyncDateFor(SystemEventType.FETCH_LAB_MESSAGES), date);
	}

	@Test
	public void testInitializeFetchEventTest() {
		SystemEventDto systemEventDto = syncFacadeEjb.startSyncFor(SystemEventType.FETCH_LAB_MESSAGES);
		// must be null for parsing the notification last update date
		assertEquals(systemEventDto.getAdditionalInfo(), null);
		assertEquals(systemEventDto.getStatus(), SystemEventStatus.STARTED);
		assertEquals(systemEventDto.getType(), SystemEventType.FETCH_LAB_MESSAGES);
	}
}
