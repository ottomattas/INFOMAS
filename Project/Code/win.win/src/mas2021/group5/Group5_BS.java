package mas2021.group5;


import java.util.List;
import java.util.Map;
import java.util.Random;

import genius.core.Bid;
import genius.core.bidding.BidDetails;
import genius.core.boaframework.NegotiationSession;
import genius.core.boaframework.OMStrategy;
import genius.core.boaframework.OfferingStrategy;
import genius.core.boaframework.OpponentModel;
import genius.core.boaframework.SortedOutcomeSpace;
import genius.core.misc.Range;
import genius.core.analysis.ParetoFrontier;
import genius.core.analysis.BidPoint;

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
	/** Phase switch time */
	private double switchTime = .2;
	/** Minimal acceptable Utility */
	private double MinUtil = .5;
	/** Opponent Model */
	public OpponentModel model;
	/** ParetoFrontier */
	public ParetoFrontier paretoFrontier;
	/** The negotiation session */
	private NegotiationSession session;
	/** Preferred bid by the opponent */
	private Bid OpponentPreferredBid;
	/** Utility of the Preferred bid by the opponent */
	double OpponentPreferredUtil = 0.0;
	
	private Random random;
	
	
	// Constants to be used in the opening phase
	// This determines the range of bids sent in this phase.
	final double openingRangeStart = 0.7;
	final double openingRangeEnd = 0.8;
	
	// Add this constant proportionally w.r.t. time/switchTime to the opening range
	final double rangeMaxMove = 0.2;
	
	/**
	 * Method which initializes the agent by setting all parameters. The
	 * parameter "e" is the only parameter which is required.
	 */
	@Override
	public void init(NegotiationSession negotationSession, OpponentModel opponentModel, OMStrategy opponentModelStrategy,
			Map<String, Double> parameters) throws Exception {
		super.init(negotationSession, parameters);
		this.negotiationSession = negotationSession;

		this.outcomespace = new SortedOutcomeSpace(negotiationSession.getUtilitySpace());
		this.negotiationSession.setOutcomeSpace(outcomespace);

		this.model = opponentModel;
		
		this.paretoFrontier = null;
		this.session = negotiationSession;
		this.outcomespace = new SortedOutcomeSpace(session.getUtilitySpace());
		this.OpponentPreferredBid = outcomespace.getBidNearUtility(1.0).getBid();
		
		this.random = new Random();
	}

	/**
	 * determineOpeningBid creates a list of bids within the range of 0.7 and 0.8 utility
	 * and selects a random bid from this created sublist, which is returned and
	 * used as opening bid. 
	 */
	@Override
	public BidDetails determineOpeningBid() {
		return this.openingPhase(0);
	}

	/**
	 * Offering strategy based on Pareto frontiers and initial confusion of the opponent.
	 * Up until switchTime is reached, openingphase() will be triggered. This function returns random bids
	 * with rising utility for this agent. After switchTime is reached, the function laterphase() is
	 * triggered, which slowly walks down in utility along the estimated Pareto Frontier until
	 * the end of the negotiation. This may be acceptance of the bid or the end of the session.
	 * 
	 * @return BidDetails
	 */
	@Override
	public BidDetails determineNextBid() {
		double time = negotiationSession.getTime();
		BidDetails nextBid;
		
		if (time <= switchTime) {
			nextBid = openingPhase(time);
		} else {
			nextBid = laterPhase(time);
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
	public BidDetails openingPhase(double time)
	{
		final double delta = this.rangeMaxMove * (time/switchTime);
		final Range range = new Range(this.openingRangeStart+delta,this.openingRangeEnd+delta);
		final List<BidDetails> bids = outcomespace.getBidsinRange(range);
		return bids.get(this.random.nextInt(bids.size()));
		
	}
	
	/**
	 * Gets the estimated Pareto frontier from the Group5_OMS.java file
	 * and make bids accordingly. Parts of the getIndexOfBidNearUtility algorithm
	 * used by SortedOutcomeSpace is also used to set the right bid.
	 * 
	 * @param time
	 * @return BidDetails
	 * 
	 * Adapted from SortedOutcomeSpace.class
	 */
	public BidDetails laterPhase(double time)
	{
		CalculatePareto();
		List<BidPoint> Pareto = paretoFrontier.getFrontier();
		final double multiplier = (time-switchTime)/(1-switchTime);
		final double utility = 1-(1-MinUtil)*multiplier;
		final int index = searchIndexWith(utility, Pareto);
		int newIndex = -1;
		double closestDistance = Math.abs(Pareto.get(index).getUtilityA() - utility);
		
		// Can be superfluous, if speed is needed
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
		} 
		if (newIndex == -1) {
			newIndex = index;
		}
		
		final BidDetails newBid = new BidDetails(Pareto.get(newIndex).getBid(),Pareto.get(newIndex).getUtilityA());
		
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
	
	/**
	 * Re-generate the Pareto Frontier
	 */
	public void CalculatePareto()
	{
		// Only recalculate the pareto frontier when there is a significant change in preferred bid
		
		if (!(OpponentPreferredUtil == model.getBidEvaluation(OpponentPreferredBid))) {
			// Cleanup the old pareto frontier. The utilities which we estimated of the opponent when we 
			// updated this the last time, might be obsolete, so we should regenerate the entire frontier.
			final ParetoFrontier paretoFrontier = new ParetoFrontier();
			
			final List<BidDetails> bids = outcomespace.getOrderedList();
			
			final int bidSize = bids.size();
			
			for (int index = 0; index < bidSize; index++) {
				final BidDetails bidDetail = bids.get(index);
				final Bid bid = bidDetail.getBid();
				double opponentUtility = model.getBidEvaluation(bid);
				double myUtility = session.getUtilitySpace().getUtility(bid);
				BidPoint bidPoint = new BidPoint(bid, myUtility, opponentUtility);
				paretoFrontier.mergeIntoFrontier(bidPoint);
		}
		};
	}
	
	@Override
	public String getName() {
		return "Group5 win.win Bidding Strategy";
	}
}