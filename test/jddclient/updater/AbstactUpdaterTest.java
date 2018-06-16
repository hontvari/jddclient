package jddclient.updater;

import static jddclient.ExampleAddress.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.net.InetAddress;

import jddclient.ExampleAddress;
import jddclient.Store;
import jddclient.updater.AbstractUpdater.TransactionState;
import jddclient.updater.ProviderException.FurtherAction;
import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Mocked;
import mockit.Tested;
import mockit.integration.junit4.JMockit;

import org.joda.time.DateTimeUtils;
import org.joda.time.Duration;
import org.joda.time.Minutes;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JMockit.class)
public class AbstactUpdaterTest {

    @Tested(availableDuringSetup=true)
    private AbstractUpdater updater;

    @Mocked
    private Store store;

    @Before
    public void initialize() {
        updater.setName("jddclient.example.org");
        updater.setStore(store);
    }

    @Test
    public void testUpdateDoesntSendSameAddress() throws SameIpException,
            UpdaterException {
        new Expectations() {
            {
                updater.sendAddress((InetAddress) any);
                times = 1;
            }
        };

        updateNoException(updater, IP);
        updateNoException(updater, IP);
    }

    /**
     * Calls {@link Updater#update} but swallows checked exceptions. Use it if
     * the sendAddress() call is checked instead of the exception.
     */
    private void updateNoException(Updater updater, InetAddress address) {
        try {
            updater.update(address);
        } catch (SkippedUpdateException e) {
            // sendAddress() call is checked instead of the exception
        } catch (UpdaterException e) {
            // sendAddress() call is checked instead of the exception
        } catch (SameIpException e) {
            // sendAddress() call is checked instead of the exception
        }
    }

    @Test
    public void testForceDoesSendSameAddress() throws UpdaterException,
            SameIpException {
        new Expectations() {
            {
                updater.sendAddress((InetAddress) any);
                times = 2;
            }
        };

        updater.force(IP);
        updater.force(IP);
    }


    @Test
    public void testDontSendAfterPermanentFailure() throws UpdaterException,
            SameIpException {
        new Expectations() {
            {
                updater.sendAddress((InetAddress) any);
                result =
                        new ProviderException("permanent",
                                FurtherAction.Permanent);
                times = 1;
            }
        };

        updateNoException(updater, IP);
        updateNoException(updater, IP);
    }

    @Test
    public void testRetryAfterTransientFailure() throws UpdaterException,
            SameIpException {
        new Expectations() {
            {
                updater.sendAddress((InetAddress) any);
                result =
                        new ProviderException("transient", FurtherAction.Retry);
                times = 2;
            }
        };

        updateNoException(updater, IP);
        updateNoException(updater, IP);
    }

    @Test
    public void testDontSendAfterRepeatedTransientFailure()
            throws UpdaterException, SameIpException {
        new Expectations() {
            {
                updater.sendAddress((InetAddress) any);
                result = new UpdaterException("the tubes are clogged up");
                times = 5;
            }
        };

        updateNoException(updater, IP);
        updateNoException(updater, IP);
        updateNoException(updater, IP);
        updateNoException(updater, IP);
        updateNoException(updater, IP);
        updateNoException(updater, IP);
    }

    @Test()
    public void testAbortOnStoreException() throws UpdaterException,
            SameIpException, SkippedUpdateException {
        new Expectations() {
            {
                store.save();
                result = new RuntimeException();
                times = 1;
            }
        };

        try {
            updater.update(IP);
            fail("An exception should have been thrown");
        } catch (RuntimeException e) {
            // already verified above
        }

        assertEquals(TransactionState.RUNNING, Deencapsulation.getField(
                updater, TransactionState.class));
    }

    @Test(expected=SkippedUpdateException.class)
    public void testAbortOnUnknownExceptionDuringSend()
            throws UpdaterException, SameIpException, SkippedUpdateException {
        new Expectations() {
            {
                updater.sendAddress((InetAddress) any);
                result = new RuntimeException();
                times=1;
            }
        };

        try {
            updater.update(IP1);
            fail("An exception should have been thrown");
        } catch (UpdaterException e) {
            // already verified above
        }

        updater.update(ExampleAddress.IP2);
    }

    @Test(expected=UpdaterException.class)
    public void testDontUpdateIfPreviousAborted() throws UpdaterException,
            SameIpException, SkippedUpdateException {
        new Expectations() {
            {
                Deencapsulation.setField(updater, TransactionState.RUNNING);
                updater.sendAddress((InetAddress) any);
                times = 0;
            }
        };

        updater.update(IP);
    }

    @Test
    public void testForceAfterAbortWorks() throws UpdaterException,
            SameIpException {
        new Expectations() {
            {
                Deencapsulation.setField(updater, TransactionState.RUNNING);
                updater.sendAddress((InetAddress) any);
                times = 1;
            }
        };

        updater.force(IP);
    }

    @Test
    public void testSameAddressErrorDontStartLoop() throws UpdaterException,
            SameIpException {
        new Expectations() {
            {
                updater.sendAddress((InetAddress) any);
                result = new SameIpException("1111111 error-record-ip-same");
                times = 1;
            }
        };

        updateNoException(updater, IP);
        updateNoException(updater, IP);
    }

    @Test
    public void testProviderRequestedBreakIsKept() throws SameIpException,
            UpdaterException {
        new Expectations() {
            {
                updater.sendAddress((InetAddress) any);
                result =
                        new ProviderMaintenanceException("resting", Minutes.minutes(30)
                                .toStandardDuration());
                times = 1;
            }
        };

        updateNoException(updater, IP);
        updateNoException(updater, IP);
    }

    @Test
    public void testProviderRequestedBreakEnded() throws SameIpException,
            UpdaterException {
        final Duration breakDuration = Minutes.minutes(30).toStandardDuration();
        new Expectations() {
            {
                updater.sendAddress((InetAddress) any);
                result = new ProviderMaintenanceException("resting", breakDuration);
                times = 2;
            }
        };

        updateNoException(updater, IP);

        DateTimeUtils.setCurrentMillisOffset(breakDuration.plus(
                Minutes.ONE.toStandardDuration()).getMillis());
        updateNoException(updater, IP);
        DateTimeUtils.setCurrentMillisSystem();
    }

}
