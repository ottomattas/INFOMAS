package mas2021.group5;

import java.util.List;

import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;

import genius.core.bidding.BidDetails;
import genius.core.boaframework.BOAparameter;
import genius.core.boaframework.NegotiationSession;
import genius.core.boaframework.OMStrategy;
import genius.core.boaframework.OpponentModel;
import genius.core.boaframework.SortedOutcomeSpace;
import genius.core.misc.Range;
import genius.core.analysis.ParetoFrontier;
import genius.core.Bid;
import genius.core.analysis.BidPoint;

/**
 * This class uses an opponent model to determine the next bid for the opponent,
 * while taking the opponent's preferences into account. The opponent model is
 * used to select the best bid.
 * 
 */
public class Group5_OMS extends OMStrategy {

	/**
	 * when to stop updating the opponentmodel. Note that this value is not
	 * exactly one as a match sometimes lasts slightly longer.
	 */
	double updateThreshold = 1.1;
	public ParetoFrontier paretoFrontier;
	SortedOutcomeSpace outcomespace;
	NegotiationSession session;

	/**
	 * Initializes the opponent model strategy. If a value for the parameter t
	 * is given, then it is set to this value. Otherwise, the default value is
	 * used.
	 * 
	 * @param negotiationSession
	 *            state of the negotiation.
	 * @param model
	 *            opponent model used in conjunction with this opponent modeling
	 *            strategy.
	 * @param parameters
	 *            set of parameters for this opponent model strategy.
	 */
	@Override
	public void init(NegotiationSession negotiationSession, OpponentModel model, Map<String, Double> parameters) {
		super.init(negotiationSession, model, parameters);
		paretoFrontier = null;
		session = negotiationSession;
		outcomespace = new SortedOutcomeSpace(session.getUtilitySpace());
		if (parameters.get("t") != null) {
			updateThreshold = parameters.get("t").doubleValue();
		} else {
			System.out.println("OMStrategy assumed t = 1.1");
		}
	}

	/**
	 * Returns the best bid for the opponent given a set of similarly preferred
	 * bids.
	 * 
	 * @param list
	 *            of the bids considered for offering.
	 * @return bid to be offered to opponent.
	 */
	@Override
	public BidDetails getBid(List<BidDetails> allBids) {

		// 1. If there is only a single bid, return this bid
		if (allBids.size() == 1) {
			return allBids.get(0);
		}
		double bestUtil = -1;
		BidDetails bestBid = allBids.get(0);

		// 2. Check that not all bids are assigned at utility of 0
		// to ensure that the opponent model works. If the opponent model
		// does not work, offer a random bid.
		boolean allWereZero = true;
		// 3. Determine the best bid
		for (BidDetails bid : allBids) {
			double evaluation = model.getBidEvaluation(bid.getBid());
			if (evaluation > 0.0001) {
				allWereZero = false;
			}
			if (evaluation > bestUtil) {
				bestBid = bid;
				bestUtil = evaluation;
			}
		}
		// 4. The opponent model did not work, therefore, offer a random bid.
		if (allWereZero) {
			Random r = new Random();
			return allBids.get(r.nextInt(allBids.size()));
		}
		return bestBid;
	}
	/*
		Re-generate the Pareto Frontier
	 */
	public void CalculatePareto()
	{
		// Cleanup the old pareto frontier. The utilities which we estimated of the opponent when we 
		// updated this the last time, might be obsolete, so we should regenerate the entire frontier.
		paretoFrontier = new ParetoFrontier();
		
		List<BidDetails> bids = outcomespace.getOrderedList();
		
		bids.forEach(new Consumer<BidDetails>() {
			@Override 
			public void accept(BidDetails bidDetail) {
				Bid bid = bidDetail.getBid();
				double opponentUtility = model.getBidEvaluation(bid);
				double myUtility = session.getUtilitySpace().getUtility(bid);
				BidPoint bidPoint = new BidPoint(bid, myUtility, opponentUtility);
				paretoFrontier.mergeIntoFrontier(bidPoint);
			}
		});
	
	}
	
	
	/*
	 * This function determines if a bid Pareto dominates another bid
	 * the arguments are the two bids, and a list of biddetails, which 
	 * should contain the preferences of the opponent, where the first
	 * element is the most preferred by the opponent, and the latest
	 * element is the least preferred by the opponent.  
	 */
	public boolean Dominates (BidDetails bid, BidDetails bid2, List<BidDetails> opponentUtility)
	{
		if (bid.compareTo(bid2) != 1)
		{
			if (opponentUtility.indexOf(bid) <= opponentUtility.indexOf(bid2))
			{
				return true;
			}
			else
			{
				return false;
			}
		}
		else
		{
			return false;
		}
	}
	
	/**
	 * Returns the Pareto-optimal frontier.
	 * 
	 * @return Pareto-optimal frontier.
	 */
	public List<BidPoint> getPareto() {
		return paretoFrontier.getFrontier();
	}
	
	/**
	 * The opponent model may be updated, unless the time is higher than a given
	 * constant.
	 * 
	 * @return true if model may be updated.
	 */
	@Override
	public boolean canUpdateOM() {
		return negotiationSession.getTime() < updateThreshold;
	}

	@Override
	public Set<BOAparameter> getParameterSpec() {
		Set<BOAparameter> set = new HashSet<BOAparameter>();
		set.add(new BOAparameter("t", 1.1, "Time after which the OM should not be updated"));
		return set;
	}

	@Override
	public String getName() {
		return "Group5 win.win Opponent Model Strategy";
	}
}