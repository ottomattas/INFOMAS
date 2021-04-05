package mas2021.group5;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import genius.core.bidding.BidDetails;
import genius.core.boaframework.BOAparameter;
import genius.core.boaframework.NegotiationSession;
import genius.core.boaframework.NoModel;
import genius.core.boaframework.OMStrategy;
import genius.core.boaframework.OfferingStrategy;
import genius.core.boaframework.OpponentModel;
import genius.core.boaframework.SortedOutcomeSpace;
import genius.core.misc.Range;

/**
 * This is an abstract class used to implement a TimeDependentAgent Strategy
 * adapted from [1] [1] S. Shaheen Fatima Michael Wooldridge Nicholas R.
 * Jennings Optimal Negotiation Strategies for Agents with Incomplete
 * Information http://eprints.ecs.soton.ac.uk/6151/1/atal01.pdf
 * 
 * The default strategy was extended to enable the usage of opponent models.
 */
public class Group5_BS extends OfferingStrategy {

	/**
	 * TODO: A lot of these things are still present from the original code we used
	 * Stuff that isn't necessary should be removed and code in general maybe cleaned
	 * up a bit. 
	 */
	/**
	 * k in [0, 1]. For k = 0 the agent starts with a bid of maximum utility
	 */
	private double k;
	/** Maximum target utility */
	private double Pmax;
	/** Minimum target utility */
	private double Pmin;
	/** Concession factor */
	private double e;
	/** Outcome space */
	private SortedOutcomeSpace outcomespace;
	/** Opponent model strategy */
	private Group5_OMS omS;
	/** Phase switch time */
	private double ts;
	/** Minimal acceptable Utility */
	private double MinUtil;

	/**
	 * Method which initializes the agent by setting all parameters. The
	 * parameter "e" is the only parameter which is required.
	 */
	@Override
	public void init(NegotiationSession negoSession, OpponentModel model, OMStrategy oms,
			Map<String, Double> parameters) throws Exception {
		super.init(negoSession, parameters);
		if (parameters.get("e") != null) {
			this.negotiationSession = negoSession;

			outcomespace = new SortedOutcomeSpace(negotiationSession.getUtilitySpace());
			negotiationSession.setOutcomeSpace(outcomespace);

			this.e = parameters.get("e");

			if (parameters.get("k") != null)
				this.k = parameters.get("k");
			else
				this.k = 0;

			if (parameters.get("min") != null)
				this.Pmin = parameters.get("min");
			else
				this.Pmin = negoSession.getMinBidinDomain().getMyUndiscountedUtil();

			if (parameters.get("max") != null) {
				Pmax = parameters.get("max");
			} else {
				BidDetails maxBid = negoSession.getMaxBidinDomain();
				Pmax = maxBid.getMyUndiscountedUtil();
			}

			this.opponentModel = model;
			
			this.omStrategy = oms;
			
			this.ts = .2;
		} else {
			throw new Exception("Constant \"e\" for the concession speed was not set.");
		}
	}

	/**
	 * determineOpeningBid creates a list of bids within the range of 0.7 and 0.8 utility
	 * and selects a random bid from this created sublist, which is returned and
	 * used as opening bid. 
	 */
	@Override
	public BidDetails determineOpeningBid() {
		Range r = new Range(0.7,0.8);
		List<BidDetails> t = outcomespace.getBidsinRange(r);
		Random Rand = new Random();
		return t.get(Rand.nextInt(t.size()));
	}

	/**
	 * Simple offering strategy which retrieves the target utility and looks for
	 * the nearest bid if no opponent model is specified. If an opponent model
	 * is specified, then the agent return a bid according to the opponent model
	 * strategy.
	 */
	@Override
	public BidDetails determineNextBid() {
		/*
		 * TODO: Create determineNextBid(). There are 2, maybe three phases, each 
		 * in seperate function. The idea is that there need to be made some bids
		 * until there is enough information to calculate 'pareto frontier', at
		 * which point it makes bids 'to the right' of the pareto frontier.
		 * Maybe at the end of a negotiation session a third phase is entered,
		 * where bids become 'worse' more quickly for us. 
		 * Might not be necessary, since acceptance condition also changes at that
		 * point for us. This method should call both openingphase() and laterphase()
		 * accordingly, (depending on the time passed)
		 */
		double time = negotiationSession.getTime();
		
		if (time >= ts) {
			nextBid = openingphase(time);
		} else {
			nextBid = laterphase(time);
		}
		
		return nextBid;
	}
	
	/**
	 * Returns random bids biding time. Moving slowly up in list of bids 
	 * still giving random bids until ts is reached.
	 * 
	 * @param time
	 * @return BidDetails
	 */
	public BidDetails openingphase(double time)
	{
		
		Range r = new Range(0.7+(ts*time),0.8+(ts*time));
		List<BidDetails> t = outcomespace.getBidsinRange(r);
		Random Rand = new Random();
		return t.get(Rand.nextInt(t.size()));
		
	}
	
	/**
	 * Gets the estimated pareto frontier from the Group5_OMS.java file
	 * and make bids accordingly. Parts of the getIndexOfBidNearUtility algorithm
	 * used by SortedOutcomeSpace is also used to set the right bid.
	 * 
	 * @param time
	 * @return BidDetails
	 */
	public BidDetails laterphase(double time)
	{
		List<BidDetails> Pareto = omS.getPareto();
		double utility = 1-MinUtil*((time-ts)/(1-ts));
		int index = searchIndexWith(utility, Pareto);
		int newIndex = -1;
		double closestDistance = Math.abs(Pareto.get(index).getMyUndiscountedUtil() - utility);

		// checks if the BidDetails above the selected is closer to the targetUtility
		if (index > 0 && Math.abs(Pareto.get(index - 1).getMyUndiscountedUtil() - utility) < closestDistance) {
			newIndex = index - 1;
			closestDistance = Math.abs(Pareto.get(index - 1).getMyUndiscountedUtil() - utility);
		}
		
		// checks if the BidDetails below the selected is closer to the targetUtility
		if (index + 1 < Pareto.size()
				&& Math.abs(Pareto.get(index + 1).getMyUndiscountedUtil() - utility) < closestDistance) {
			newIndex = index + 1;
			closestDistance = Math.abs(Pareto.get(index + 1).getMyUndiscountedUtil() - utility);
		} else
			newIndex = index;
		
		return Pareto.get(newIndex);
		
	}

	/**
	 * From [1]:
	 * 
	 * A wide range of time dependent functions can be defined by varying the
	 * way in which f(t) is computed. However, functions must ensure that 0 <=
	 * f(t) <= 1, f(0) = k, and f(1) = 1.
	 * 
	 * That is, the offer will always be between the value range, at the
	 * beginning it will give the initial constant and when the deadline is
	 * reached, it will offer the reservation value.
	 * 
	 * For e = 0 (special case), it will behave as a Hardliner.
	 */
	public double f(double t) {
		if (e == 0)
			return k;
		double ft = k + (1 - k) * Math.pow(t, 1.0 / e);
		return ft;
	}

	/**
	 * Makes sure the target utility with in the acceptable range according to
	 * the domain. Goes from Pmax to Pmin!
	 * 
	 * @param t
	 * @return double
	 */
	public double p(double t) {
		return Pmin + (Pmax - Pmin) * (1 - f(t));
	}

	public NegotiationSession getNegotiationSession() {
		return negotiationSession;
	}
	
	/**
	 * Binary search of a BidDetails with a particular value if there is no
	 * BidDetails with the exact value gives the last index because this is the
	 * closest BidDetails to the value
	 * 
	 * @param value
	 * @return index
	 * 
	 * Source: SortedOutcomeSpace.class
	 */
	public int searchIndexWith(double value, List<BidDetails> listBids) {
		
		int middle = -1;
		int low = 0;
		int high = listBids.size() - 1;
		int lastMiddle = 0;
		while (lastMiddle != middle) {
			lastMiddle = middle;
			middle = (low + high) / 2;
			if (listBids.get(middle).getMyUndiscountedUtil() == value) {
				return middle;
			}
			if (listBids.get(middle).getMyUndiscountedUtil() < value) {
				high = middle;
			}
			if (listBids.get(middle).getMyUndiscountedUtil() > value) {
				low = middle;
			}
		}
		return middle;
	}

	@Override
	public Set<BOAparameter> getParameterSpec() {
		Set<BOAparameter> set = new HashSet<BOAparameter>();
		set.add(new BOAparameter("e", 1.0, "Concession rate"));
		set.add(new BOAparameter("k", 0.0, "Offset"));
		set.add(new BOAparameter("min", 0.0, "Minimum utility"));
		set.add(new BOAparameter("max", 0.99, "Maximum utility"));

		return set;
	}

	@Override
	public String getName() {
		return "Group5 win.win Bidding Strategy";
	}
}