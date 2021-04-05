package mas2021.group5;


import java.util.List;
import java.util.Map;
import java.util.Random;

import genius.core.analysis.*;
import genius.core.bidding.BidDetails;
import genius.core.boaframework.NegotiationSession;
import genius.core.boaframework.OMStrategy;
import genius.core.boaframework.OfferingStrategy;
import genius.core.boaframework.OpponentModel;
import genius.core.boaframework.SortedOutcomeSpace;
import genius.core.misc.Range;

/**
 * This is an abstract class used to implement the bidding strategy for Group 5
 * win.win bidding strategy. This strategy requires the use of the Group5_OMS.java
 * Opponent model strategy, due to implementation of a Pareto Frontier.
 * 
 * The default strategy was extended to enable the usage of the opponent model strategy.
 */
public class Group5_BS extends OfferingStrategy {

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
		this.negotiationSession = negoSession;

		outcomespace = new SortedOutcomeSpace(negotiationSession.getUtilitySpace());
		negotiationSession.setOutcomeSpace(outcomespace);

		this.opponentModel = model;
		
		this.omStrategy = oms;
		
		this.ts = .2;
		
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
	 * Offering strategy based on Pareto forntiers and initial confusion of the opponent.
	 * Up until ts is reached, openingphase() will be triggered. This function returns random bids
	 * with rising utility for this agent. After ts is reached, the function laterphase() is
	 * triggered, which slowly walks down in utility along the estimated Pareto Frontier until
	 * the end of the negotiation. This may be acceptance of the bid or the end of the session.
	 * 
	 * @return BidDetails
	 */
	@Override
	public BidDetails determineNextBid() {
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
	 * Gets the estimated Pareto frontier from the Group5_OMS.java file
	 * and make bids accordingly. Parts of the getIndexOfBidNearUtility algorithm
	 * used by SortedOutcomeSpace is also used to set the right bid.
	 * 
	 * @param time
	 * @return BidDetails
	 * 
	 * Source: SortedOutcomeSpace.class
	 */
	public BidDetails laterphase(double time)
	{
		List<BidPoint> Pareto = omS.getPareto();
		double utility = 1-MinUtil*((time-ts)/(1-ts));
		int index = searchIndexWith(utility, Pareto);
		int newIndex = -1;
		double closestDistance = Math.abs(Pareto.get(index).getUtilityA() - utility);

		// checks if the BidDetails above the selected is closer to the targetUtility
		if (index > 0 && Math.abs(Pareto.get(index - 1).getUtilityA() - utility) < closestDistance) {
			newIndex = index - 1;
			closestDistance = Math.abs(Pareto.get(index - 1).getUtilityA() - utility);
		}
		
		// checks if the BidDetails below the selected is closer to the targetUtility
		if (index + 1 < Pareto.size()
				&& Math.abs(Pareto.get(index + 1).getUtilityA() - utility) < closestDistance) {
			newIndex = index + 1;
			closestDistance = Math.abs(Pareto.get(index + 1).getUtilityA() - utility);
		} else
			newIndex = index;
		
		BidDetails newBid = new BidDetails(Pareto.get(newIndex).getBid(),Pareto.get(newIndex).getUtilityA());
		
		return newBid;
		
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
	public int searchIndexWith(double value, List<BidPoint> listBids) {
		
		int middle = -1;
		int low = 0;
		int high = listBids.size() - 1;
		int lastMiddle = 0;
		while (lastMiddle != middle) {
			lastMiddle = middle;
			middle = (low + high) / 2;
			if (listBids.get(middle).getUtilityA() == value) {
				return middle;
			}
			if (listBids.get(middle).getUtilityA() < value) {
				high = middle;
			}
			if (listBids.get(middle).getUtilityA() > value) {
				low = middle;
			}
		}
		return middle;
	}

	@Override
	public String getName() {
		return "Group5 win.win Bidding Strategy";
	}
}